
function ensure_no_uncommited_changes(){
  git_status=$(git status --porcelain)
  if [[ "$git_status" != "" ]]; then
    echo "You have uncommitted changes:" 1>&2
    echo "$git_status" 1>&2
    echo "Commit or clean up before building" 1>&2
    exit 1
  fi
}

