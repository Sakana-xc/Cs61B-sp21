## Acknowledgement
Course taught: CS61B spring 2021 Data structures

Projects/labs/notes

Link: https://sp21.datastructur.es/

Instructor: Josh Hug (the best)

# Gitlet

Project2 Gilet is a git-like version control system features all local git key functions.

I put a lot of effort in it and learned a lot through the process so I am kinda pround of it.


## Algorithms

__Each file in your working directory can be in 
one of two states: tracked or untracked.__

* __Tracked files__ -- that Gitlet knows about it.
    - Files has been added and staged.
    - File is modified if:
      - present in working directory, contents
        different than in head commit, and not staged.
      - present in working directory, has been staged,
        but file contents in stage area is different than 
        in working directory. (dirty state)
      - absent in working directory, but in stage added.
      - absent in working directory, present in head commit,
        but not in stage removed.

* __Untracked files__
    - Files in current working area, but not in last commit(head)
      and hasen't yet been staged.

* __Objects__
  - Stage: represent a staging area, contains ```HashMap<String filename, String blobId> added ```;
           ```HashSet<String filename> removed ```
  - Commit:  represent a commit object, contains commitId, it's parents,
              tracked files, timeStamp, and commit message. |(SHA1)
  - Blob:   represent a blob object contains blobId |(SHA1(filename,contents)), filename(```join(CWD,filename)```),
            ```byte [] content```

* __Merge__
             
![cs61b-141](https://user-images.githubusercontent.com/41518197/212537778-15fa67fb-5971-4733-be5e-287e5b8d83b3.jpg)


            
           
    
  


## How to run

__Compile with__

```bash
  gitlet/*.java
```
__Everything is set! run__
```bash
  java gitlet.Main [command]
```
__Gitlet features keyfunctions of local Git__
(make sure init before use other command)
```bash
   init
   add [file name] 
   commit [message]
   rm [file name]
   log
   global-log
   status
   find [commit message]
   branch [branch name]
   rm-branch [branch name]
   checkout -- [filename]
   checkout [commit id] -- [file name]
   checkout [branch name]
   reset [commit id]
   merge [branch name]
   ```
   



## Persistence

```bash
.gitlet
    -- blobs
    -- commits
	-- staging
	-- [stage]
    -- refs
		-- heads -> [master][branch name]
	-- [HEAD]
```

```blobs directory```: stores all tracked(committed) file(name:blobId, content:blob.content)

```commits directory```: stores all commit objects

```Staging```: stores added file(filename, blobId), removed filename

```stage```: stage object

```heads ```: stores branches, with branch name and a reference to commit

```HEAD```: a pointer reference current branch name if points at






