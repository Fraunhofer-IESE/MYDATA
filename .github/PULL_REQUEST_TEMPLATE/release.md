## Release candidate

This PR prepares the next MYDATA release and starts the next development iteration.

### Review checklist

- [ ] The PR contains exactly two commits: the release version and the next SNAPSHOT version.
- [ ] Only the intended POM version changes are included.
- [ ] All required PR checks passed.

### Merge instructions

**Use "Create a merge commit"! Do NOT squash or rebase!**

- [ ] Confirm that **"Create a merge commit"** is selected before merging.

The original release commit (first of the two commits) must remain part of the `main` history.

### After merging

Run **Perform MYDATA Release** manually with the full SHA of the first commit.

Publish the GitHub draft release only after Maven Central publishing has succeeded.
