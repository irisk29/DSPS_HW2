## Statistics With\Without Combiner:

1. First Map-Reduce Step - Counting N3, C1 and C2:

   |                               | With combiners | Without combiners |
   |-------------------------------|----------------|-------------------|
   | Map input records             | 163,471,963    | 163,471,963       |
   | Map output records            | 490,415,889    | 71,119,513        |
   | Map output bytes              | 13,358,144,882 | 2,400,202,188     |
   | Combine input records         | 533,086,252    | None              |
   | Combine output records        | 78,811,679     | None              |
   | Reduce input records          | 36,141,316     | 71,119,513        |
   | Reduce output records         | 21,484,745     | 1,686,118         |


2. Second Map-Reduce Step - Counting C0, N1 and N2:

   |                               | With combiners | Without combiners |
   |-------------------------------|----------------|-------------------|
   | Map input records             | 21,484,745     | 1,686,118         |
   | Map output records            | 64,454,235     | 3,372,236         |
   | Map output bytes              | 4,315,686,196  | 57,328,012        |
   | Combine input records         | 87,305,040     | None              |
   | Combine output records        | 48,395,652     | None              |
   | Reduce input records          | 25,544,847     | 3,372,236         |
   | Reduce output records         | 21,484,745     | 6,881             |


3. In the Third Step we use only Map. Therefore, there is no communication between mappers and reduces.

   |                               | With combiners | Without combiners |
   |-------------------------------|----------------|-------------------|
   | Map input records             | 21,484,745     | 1,686,118         |
   | Map output records            | 21,484,745     | 3,372,236         |

We've reached the conclusion that using local aggregation lowers the network traffic but uppers the map output bytes
size in contrast to running without local aggregation where the network traffic is higher, but the map output bytes size
is lower. Both programs ran approximately at a similar time, so we would recommend using each method according to the
developer needs.
