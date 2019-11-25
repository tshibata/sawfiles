operation = {
	inside "build/tmp/zips.tar", {
		list(glob("**/*.zip")) each {zip ->
			inside zip, {
				list(glob("**/tmp")) each {tmp ->
					execute("rm", "-fr", tmp)
				}
			}
		}
	}
}
