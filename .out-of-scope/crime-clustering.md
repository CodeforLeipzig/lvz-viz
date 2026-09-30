# Crime clustering graph

lvz-viz does not build a second visualisation that clusters crimes by similarity and draws them as a graph.

## Why this is out of scope

The request asks for vector-space clustering of articles, multi-dimensional scaling or thresholding, and a graph view of the result.
That is a research project of its own rather than a feature of this app: it needs a text representation, tuning of the clustering, and a way to explain to readers what an edge between two crimes means.

The app's focus is showing where and when police ticker incidents happened.
The search map, the statistics heatmap and the date slider cover that, and similarity between crimes is not part of it.
A clustering experiment fits better as a separate notebook or project that uses the lvz-viz API as a data source.

## Prior requests

- #11: "Cluster crime"
