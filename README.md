# sawfiles
A DSL to modify files inside archive files

## Usage
For example, when you want to remove tmp directories in zip files in a tar file:
```
operation = {
	inside "zips.tar", {
		list(glob("**/*.zip")) each {zip ->
			inside zip, {
				list(glob("**/tmp")) each {tmp ->
					execute("rm", "-fr", tmp)
				}
			}
		}
	}
}
```
