#!/bin/bash
COMMIT_MESSAGE="$(git log --oneline -1 | cut -d " " -f2-)"
grunt build
git add -A dist && git commit -m "Distribution: $COMMIT_MESSAGE"
git push origin master
grunt buildcontrol:pages --mymessage="$COMMIT_MESSAGE"