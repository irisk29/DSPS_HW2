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
- ### Step One, Counting N3, C1 and C2:
  In this step we are counting the number of times `(w1, w2, w3)` occurs, the number of times `w2` occurs and the number of times `(w1, w2)` occurs.
  For each `(w1, w2, w3)` in the 3-gram dataset, we emit in the mapper <`(w1, w2, w3)`, (trigram amount - received from the dataset)>, <`(w1, w2, ~)`, 1> and <`(~, w2, ~)`, 1>.
  Hadoop's environment sort the key-values by our compare-to function. The compare-to function first sort it by w2, then by w1 and finally by w3. We have defined that ~ is smaller then everything. In that way, the reducers will get the key-values pairs in the following order: `(~, w2, ~)`, then `(w1, w2, ~)` and lastly `(w1, w2, w3)`.
  For example, if we had the following two trigrams - `(קפה, נמס, עלית)`, `(ילד, טוב, מאוד)`, the way our key-values pairs will get to the reducer is
  ```
      (~, טוב, ~)
      (~ ,ילד, טוב)
      (ילד, טוב, מאוד)
      (~, נמס, ~)
      (~ ,קפה, נמס)
      (קפה, נמס, עלית)
  ```
  In the reducers, we store local variables, which save us the C1 and C2 for a specific `w2` and `(w1, w2)` respectively. Everytime we get `(w1 ,w2, w3)` in the reducer we emit <`(w1 ,w2, w3)`, (received N3, saved C1, saved C2)>, and everytime we get `(w1 ,w2, ~)` or `(~ ,w2, ~)` we will update C1 or C2.
  We ensure that each reducer will get all the relevant key-value pairs for a specific trigram. We do so in the Partitioner, as we defined there that every trigram with the same second word hash-code will send to the same reducer.
- ### Step Two, Counting C0, N1 and N2:
  In this step we are counting the total number of word instances in the corpus, the number of times `w3` occurs and the number of times `(w2, w3)` occurs.
  For each `(w1, w2, w3)` and (N3, C1, C2) from previous step, we add N3 * 3 to global counter that sum the total number of word instances in the corpus (C0). In addition, we emit in the mapper <`(~, w2, w3)`, 1>, <`(~, ~, w3)`, 1> and <`(w1, w2, w3)`, (previous N3, C1 and C2)>.
  Hadoop's environment sort the key-values by our new compare-to function. This compare-to function first sort it by w3, then by w2 and finally by w1. We have defined that ~ is smaller then everything. In that way, the reducers will get the key-values pairs in the following order: `(~, ~, w3)`, then `(~, w2, w3)` and lastly `(w1, w2, w3)`.
  In the reducers, we store local variables, which save us the N1 and N2 for a specific `w3` and `(w2, w3)` respectively. Everytime we get `(w1 ,w2, w3)` in the reducer we emit <`(w1 ,w2, w3`, (previous N3, previous C1, previous C2, global C0, saved N1, saved N2)>, and everytime we get `(~, w2, w3)` or `(~, ~, w3)` we will update N1 or N2.
  As in the previous step, we ensure that each reducer will get all the relevant key-value pairs for a specific trigram. We do so in the Partitioner, as we defined there that every trigram with the same third word hash-code will send to the same reducer.
- ### Step Three, Calculating Formula and Sorting Results:
  For the last step we used only mapper side, which calculating the probability function for each trigram and sort the results by `(w1,w2)` and the trigram probability.

## Scalability
We considered the scalability matter by the following aspects:
1. no-memory requirement - Instead of saving certain data for trigrams in the memory, we split the calculations in a smart way that allows us to sort the key-value pairs. The records will always arrive in order that require only O(1) memory usage. No matter what size corpus will be, our program can handle it because it does not has any memory size assumptions.
2. Increasing the number of workers (if needed) - in order to speed up the proccess, we could increase the number of insatnces by our need.

## Technical stuff
- We used instance type of M5Xlarge for all instances.
