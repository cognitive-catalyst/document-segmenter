# document-segmenter

A configurable segmenter to help break large sections (PAUs) into smaller ones for ingestion.


## Requirements

To install this project, you will need:

- [Java 8 SDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

### Java 8

1. Download and install the binary relevant to your machine.
2. Edit your `~/.bash_profile` to include the line
```
    export JAVA_HOME=/path/to/java8
```

## Basic Usage

Input is html files. Source folder: `html/source` output folder: `html/segmented`

This program will perform a recursive binary division on large PAUs. A PAU is defined as the text between two headers (or the last header and the end of document). Care has been taken to not divide tables or lists.

The target size is configurable by editting the config.properties file. The property sliceSize determines the maximum size (in characters) a PAU can be before an attempt to cut it is made. The property minimumSectionSize is used to limit how small of a PAU will be created. This is useful to prevent the creation of PAUs that are a single short paragraph.

## Algorithm Details
The size (character length) of each PAU is calculated. If the size is larger than the defined sliceSize, an attempt to cut the PAU is made. Cuts are always made along paragraph lines and as close to the center of the PAU as possible. The resulting two PAUS are then reexamined to determine if they need to be sliced further. Lists and tables are counted in the total length of a PAU but will not be sliced.

## Examples

Exampe 1:
PAU is made up of 6 paragraphs of lengths 300, 500, 400, 700, 200, 300 for a total length of 2400. sliceSize = 1000, minimumSectionSize = 500.
Pass 1 will determine that the PAU needs to be sliced and will do so as close to the center (1200) as possible. The start of paragraph 4 is at 1200 so the slice will be made here.
After pass 1 we now have 2 PAUs. PAU 1a consists of 3 paragraphs of length 300, 500, 400. PAU 1b consists of 3 paragraphs of length 700, 200, 300.
Pass 2 will determine that PAU 1a has a total length of 1200 and should be sliced. An attempt is made to slice before paragraph 2 but this would result in a PAU of length less than 500 and the attempt fails.
Pass 2 will then inspect PAU 1b and determine that it too needs to be cut (total length 1200). The slice is made before paragraph 2.
After pass 2 we will have 3 PAUs. PAU 1a remains unchanged with 3 paragraphs of length 300, 500, 400. PAU 2a is new and consists of 1 paragraph of length 700. PAU 2b is also new and consists of 2 paragraphs of length 200, and 300.
Pass 3 will inspect PAUs 2a and 2b, determine that both are below the sliceSize and the algorithm ends.

Example 2:
PAU is made up of a paragraph of 100, table of 700, paragraph of 500.
Pass 1 calcualtes a total length of 1300 and finds that between the table and final paragraph is closest to the center. A slice is made.
After pass 1 we have 2 PAUs. PAU 1a consists of a paragraph of length 100 and table of 700. PAU 1b is a paragraph of 500.
Pass 2 determines than no more divisions are needed.
Note that if paragraph 3 had been any smaller no slices would have been made.

## Maintainer
Andrew Ayres, afayres@us.ibm.com
