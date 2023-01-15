
# Gitlet

Gilet is a git-like version control system features all local git key functions.

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

            
           
    
  


## How to run

__To solely clone Gitlet__

```bash
  git clone --no-checkout https://github.com/Sakana-xc/Cs61B-sp21.git
```

__Navigate into the repository directory__

```bash
  cd Cs61B-sp21/
```

__Enable the sparse checkout feature__

```bash
  git config core.sparsecheckout true
```
__Specify the directory you want to check out__

```bash
  echo proj2  >> .git/info/sparse-checkout
```
__Check out the files in the specified directory__
```bash
  git checkout [branch name]
```
__cd into proj2, compile with__
```bash
  gitlet/*.java
```
__Now everything is set! run__
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



