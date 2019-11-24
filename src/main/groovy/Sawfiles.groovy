import java.nio.file.Files

class Sawfiles {
	File workspace

	Sawfiles(File workspace) {
		this.workspace = workspace
	}

	def static fileTypes = [
		".tar": [{file -> ["tar", "xf", file]}, {file -> ["tar", "cf", file, "."]}],
		".zip": [{file -> ["unzip", file]}, {file -> ["zip", file, "-R", "*"]}]
	]

	void operate(String path, String extension, Closure closure) {
		def abs = new File(workspace, path).getAbsoluteFile().getPath()
		def w = Files.createTempDirectory("sawfiles-").toFile()
		new ProcessBuilder(fileTypes[extension][0](abs)).directory(w).start().waitFor()
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.delegate = new Sawfiles(w)
		closure()
		new ProcessBuilder(fileTypes[extension][1](abs)).directory(w).start().waitFor()
	}

	void inside(String path, Closure closure) {
		operate(path, path.substring(path.lastIndexOf(".")), closure)
	}

	void inside(Map files, Closure closure) {
		files.each { path, extension ->
			operate(path, extension, closure)
		}
	}

	void execute(String... args) {
		new ProcessBuilder(args).directory(workspace).start().waitFor()
	}

	String toString() {
		return "Sawfiles(" + workspace + ")"
	}

    static void main(String[] args) {
		GroovyShell shell = new GroovyShell()
		Script script = shell.parse(new java.io.FileReader(args[0]))

		script.run()
		script.operation.delegate = new Sawfiles(new File(System.getProperty("user.dir")))
		script.operation()
	}
}

