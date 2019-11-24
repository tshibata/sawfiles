operation = { ->
	inside "build/tmp/a.zip", {
		execute("touch", "newfile")
	}
}
