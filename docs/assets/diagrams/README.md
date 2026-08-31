# PlantUML diagrams

The architecture diagram is maintained as
[degree-progress-architecture.puml](degree-progress-architecture.puml). The
checked-in SVG is generated from that source and is the version embedded in the
developer guide.

## Prerequisites

Java SE 25 is already required by the project. No separate Graphviz install is
needed for this diagram because the source enables PlantUML's Smetana layout.

Download the official PlantUML JAR from the
[PlantUML download page](https://plantuml.com/download). The repository's
rendering helper uses PlantUML 1.2026.7 by default and stores the downloaded
JAR in the operating system temporary directory rather than committing it to
the repository.

## Regenerate the SVG

From the project root, run:

    powershell -ExecutionPolicy Bypass -File docs/assets/diagrams/render-diagrams.ps1

The helper downloads the official JAR if it is not already present, then runs
PlantUML with -tsvg and -nometadata and writes
degree-progress-architecture.svg beside the source file. PlantUML documents
-tsvg as its SVG output option in the
[command-line guide](https://plantuml.com/command-line).
