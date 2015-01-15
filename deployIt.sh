#!/bin/bash
COMMIT_MESSAGE = git log --oneline -1 | cut -d " " -f2-
grunt build
git add dist && git commit -m "DISTRIBUTION $COMMIT_MESSAGE" 
git push origin master
grunt buildcontrol:pages