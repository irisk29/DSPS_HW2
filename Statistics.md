## Statistics With\Without Combiner:

1. First Map-Reduce Step - Counting N3, C1 and C2:

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


2. Second Map-Reduce Step - Counting C0, N1 and N2:

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


3. In the Third Step we use only Map. Therefore, there is no communication between mappers and reduces.

We've reached the conclusion that using local aggregation lowers the network traffic but uppers the map output bytes
size in contrast to running without local aggregation where the network traffic is higher, but the map output bytes size
is lower. Both programs ran approximately at a similar time, so we would recommend using each method according to the
developer needs.
