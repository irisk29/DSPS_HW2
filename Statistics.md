## Statistics With\Without Combiner:

1. First Map-Reduce Step - Counting N3, C1 and C2:

   |                               | With combiners | Without combiners |
   |-------------------------------|----------------|-------------------|
   | Map input records             | 163,471,963    | 163,471,963       |
   | Map output records            | 490,415,889    | 490,415,889       |
   | Map output bytes              | 13,358,144,882 | 13,358,144,882    |
   | Combine input records         | 533,086,252    | None              |
   | Combine output records        | 78,811,679     | None              |
   | Reduce input records          | 36,141,316     | 490,415,889       |
   | Reduce output records         | 21,484,745     | 21,484,745        |


2. Second Map-Reduce Step - Counting C0, N1 and N2:

   |                               | With combiners | Without combiners |
   |-------------------------------|----------------|-------------------|
   | Map input records             | 21,484,745     | 21,484,745        |
   | Map output records            | 64,454,235     | 64,454,235        |
   | Map output bytes              | 4,315,686,196  | 4,315,686,196     |
   | Combine input records         | 87,305,040     | None              |
   | Combine output records        | 48,395,652     | None              |
   | Reduce input records          | 25,544,847     | 64,454,235        |
   | Reduce output records         | 21,484,745     | 21,484,745        |


3. Third Map-Reduce Step - Calculating Probability:

   |                               | With combiners | Without combiners |
   |-------------------------------|----------------|-------------------|
   | Map input records             | 21,484,745     | 21,484,745        |
   | Map output records            | 21,484,745     | 21,484,745        |
   | Map output bytes              | 734,811,311    | 734,811,311       |
   | Combine input records         | None           | None              |
   | Combine output records        | None           | None              |
   | Reduce input records          | 21,484,745     | 21,484,745        |
   | Reduce output records         | 21,484,745     | 21,484,745        |

We've reached the conclusion that using local aggregation lowers the network traffic and therforce, lowers the proccess time as well. Both programs ran approximately at a similar time, but with combiner is a little faster. So, we would recommend using each method according to the developer needs.
