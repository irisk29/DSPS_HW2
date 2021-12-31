# DSPS_HW2
In this assignment we will generate a knowledge-base for Hebrew word-prediction system, based on Google 3-Gram Hebrew dataset, using Amazon Elastic Map-Reduce (EMR).
The produced knowledge-base indicates for each pair of words the probability of their possible next words.

## How to run our project
1. Copy and paste your credentials into ~/.aws/credentials.
2. Create buckets in s3 for the output & log files.
3. Create `EMRWordPrediction.jar` and upload it to s3.
4. Create a file `TrigramWordPrediction/info.json` -
  The content of the file should be
    ```
    {
      "with-combiner" : "<true to run with combiner, anything else to run without combiner>",
      "output-path" : "<s3 output bucket path you created earlier>",
      "log-path" : "<s3 log bucket path you created earlier>",
      "jar-path" : "<jar file location path in s3>",
      "main-class" : "<main class of the jar file>",
      "num-of-instances" : "<number of instances to use. Can be MAX 19>",
      "key-name" : "<name of the keyPair>"
    }
   ```
5. run TrigramWordPrediction jar using the command `java -jar TrigramWordPrediction.jar`.

## Map-Reduce Steps
1. Step One, Counting N3, C1 and C2:
In this step we are counting the number of times (w1, w2, w3) occurs, the number of times w2 occurs and the number of times (w1, w2) occurs.
For each (w1, w2, w3) in the 3-gram dataset, we emit in the mapper <(w1, w2, w3), (trigram amount - received from the dataset)>, <(w1, w2, ~), 1> and <(~, w2, ~), 1>.
Hadoop's environment sort the key-values by our compare-to function. The compare-to function first sort it by w2, then by w1 and finally by w3. We have defined that ~ is smaller then everything. In that way, the reducers will get the key-values pairs in the following order: (~, w2, ~), then (w1, w2, ~) and lastly (w1, w2, w3).
For example, if we had the following two trigrams - (קפה, נמס, עלית), (ילד, טוב, מאוד), the way our key-values pairs will get to the reducer is
```
    (~, טוב, ~)
    (~ ,ילד, טוב)
    (ילד, טוב, מאוד)
    (~, נמס, ~)
    (~ ,קפה, נמס)
    (קפה, נמס, עלית)
```
In the reducers, we store local variables, which save us the C1 and C2 for a specific w2 and (w1, w2) respectively. Everytime we get (w1 ,w2, w3) in the reducer we emit <(w1 ,w2, w3), (received N3, saved C1, saved C2)>, and everytime we get (w1 ,w2, ~) or (~ ,w2, ~) we will update C1 or C2.
We ensure that each reducer will get all the relevant key-value pairs for a specific trigram. We do so in the Partitioner, as we defined there that every trigram with the same second word hash-code will send to the same reducer.
2. Step Two, Counting C0, N1 and N2:
In this step we are counting the total number of word instances in the corpus, the number of times w3 occurs and the number of times (w2, w3) occurs.
For each (w1, w2, w3) and (N3, C1, C2) from previous step, we add N3 * 3 to global counter that sum the total number of word instances in the corpus (C0). In addition, we emit in the mapper <(~, w2, w3), 1>, <(~, ~, w3), 1> and <(w1, w2, w3), (previous N3, C1 and C2)>.
Hadoop's environment sort the key-values by our new compare-to function. This compare-to function first sort it by w3, then by w2 and finally by w1. We have defined that ~ is smaller then everything. In that way, the reducers will get the key-values pairs in the following order: (~, ~, w3), then (~, w2, w3) and lastly (w1, w2, w3).
In the reducers, we store local variables, which save us the N1 and N2 for a specific w3 and (w2, w3) respectively. Everytime we get (w1 ,w2, w3) in the reducer we emit <(w1 ,w2, w3), (previous N3, previous C1, previous C2, global C0, saved N1, saved N2)>, and everytime we get (~, w2, w3) or (~, ~, w3) we will update N1 or N2.
As in the previous step, we ensure that each reducer will get all the relevant key-value pairs for a specific trigram. We do so in the Partitioner, as we defined there that every trigram with the same third word hash-code will send to the same reducer.

## Considerations
### Scalability
We considered the scalability matter by the following aspects:
1. Thread pool - we did not use a TPS (Thread Per Client) methodology because we cannot assign TPS when we have a large amount of client, for example 1 billion clients. Using the thread pool we assigned the maximum number of threads we can in order to process the local application requests simultaneously.
2. Increasing the number of workers (if needed) - when we receive a job from a local application and n, we calculate the number of needed workers and create them accordingly. This way we scale-up when only it is needed (of course we took in considereation the limitation of 8 ec2 instances we have but potentially our program can increase to an higher numer to support 1 billion of clients for example).
3. We used linear amount of SQS - we created N SQS when N is the number of local application. This way we ensure we do not use too much resources than necessary.
4. We used two S3 buckets - we tried to ensure we do not use too much resources than necessary.
5. We used linear amount of memory in the manager - we downloaded only the input files of the local applications but not the final files which are used in the summary file that will be sent to an appropriate local application. This way we tried to ensure that the memory of the manager won't run out easily.
### Persistence
- We're catching all possible exeptions that might raise from the operation of our code.
- Visibility Timeout: using this mechanism, each message that is hanled by the manager or the workers have limited time in which it is assigned to appropriate ec2 instance. During this time if the ec2 instance that is handling the message, terminates suddenly or stalls, when the visibility timeout is reached the message is back to be visible again to all the instances. This way other worker can handle the task.
- FIFO SQS + Message Duplication ID: this way we can ensure there are no duplications. So if the commincation broke and a message sent twice we will discard the duplication.
### Threads in our application
Only the Manager uses more then one thread:
- `proccess workers finished tasks thread`: responsible for receiving finished tasks from the workers and append them to the relevant summery file. Once a summery file is full with all the results, it sends it to the appropriate local application's SQS. 
- `waiting for new local application job thread`: This is the main thread of the manager. It waits for new local application job. A job is an .txt file which every line contains an operation to perform over a link. For each one it gets, it downloads the file locally and passes it to the thread pool, there a thread will be assigned to handle this job. After that, it returns to the beginning and waits for new jobs from the local applications.
When a termination message is received it acivates the termination flow as described in that section.
- `handle new local application job thread`: Each thread in the thread pool receive a job which is a file as described above. First it checks if we need more workers than we currently have to proccess the new job. If we do, it creates the necessary amount of workers. After that, it iterates over the file, and from each line it creates a new task for the workers to handle.
### Limitation
- Number of EC2 clients: we ensure that we never creates more than <aws limitation number> EC2 clients.
- 100$ in the account: we mostly used the free tier resources to keep up with the budget and used the appropriate resources that can support the task we gave and did not choose randomly with financial considerations.

## Technical stuff
- We used instance type of M5Xlarge for all instances.
