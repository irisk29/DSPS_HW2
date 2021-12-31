## Statistics With | Without Combiners:

- We did it on `AWS cluster` with `emr-6.2.0` configuration (the latest configuration at this moment)
  with `Hadoop v3.2.1` and with 3 instances of `M5.XLarge`, it took about 6 minutes with combiners and also without them
  to perform this task.

1. Map-Reduce Job1DivideCorpus:

   |                               | With combiners | Without combiners |
   |-------------------------------|----------------|-------------------|
   | Map input records             | 163,471,963    | 163,471,963       |
   | Map output records            | 71,119,513     | 71,119,513        |
   | Map output bytes              | 2,400,202,188  | 2,400,202,188     |
   | Combine input records         | 71,119,513     | Non existing      |
   | Combine output records        | 3,372,257      | Non existing      |
   | Map output materialized bytes | 42,386,294     | 307,746,702       |
   | Reduce input records          | 3,372,257      | 71,119,513        |
   | Reduce output records         | 1,686,118      | 1,686,118         |


2. Map-Reduce Job2CalcT_rN_r:

   |                               | With combiners | Without combiners |
   |-------------------------------|----------------|-------------------|
   | Map input records             | 1,686,118      | 1,686,118         |
   | Map output records            | 3,372,236      | 3,372,236         |
   | Map output bytes              | 84,305,900     | 57,328,012        |
   | Combine input records         | 3,372,236      | Non existing      |
   | Combine output records        | 13,995         | Non existing      |
   | Map output materialized bytes | 144,353        | 10,482,224        |
   | Reduce input records          | 13,995         | 3,372,236         |
   | Reduce output records         | 6,881          | 6,881             |


3. Map-Reduce Job3JoinTriGramsWithT_rN_r:

   |                               | Without combiners |
   |-------------------------------|-------------------|
   | Map input records             | 1,692,999         |
   | Map output records            | 3,379,117         |
   | Map output bytes              | 118,694,754       |
   | Combine input records         | Non existing      |
   | Combine output records        | Non existing      |
   | Map output materialized bytes | 52,550,247        |
   | Reduce input records          | 3,379,117         |
   | Reduce output records         | 3,372,236         |


4. Map-Reduce Job4CalcProb:

   |                               | Without combiners |
   |-------------------------------|-------------------|
   | Map input records             | 3,372,236         |
   | Map output records            | 3,372,236         |
   | Map output bytes              | 138,784,160       |
   | Combine input records         | Non existing      |
   | Combine output records        | Non existing      |
   | Map output materialized bytes | 45,672,812        |
   | Reduce input records          | 3,372,236         |
   | Reduce output records         | 1,686,118         |


5. Map-Reduce Job5Sort:

   |                               | Without combiners |
   |-------------------------------|-------------------|
   | Map input records             | 1,686,118         |
   | Map output records            | 1,686,118         |
   | Map output bytes              | 55,903,136        |
   | Combine input records         | Non existing      |
   | Combine output records        | Non existing      |
   | Map output materialized bytes | 31,454,674        |
   | Reduce input records          | 1,686,118         |
   | Reduce output records         | 1,686,118         |

We've reached the conclusion that using local aggregation lowers the network traffic but uppers the map output bytes
size in contrast to running without local aggregation where the network traffic is higher, but the map output bytes size
is lower. Both programs ran approximately at a similar time, so we would recommend using each method according to the
developer needs.
