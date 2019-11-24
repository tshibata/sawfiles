operation = { ->
	inside "build/tmp/a.tar", {
		execute("touch", "newfile")
	}
}
