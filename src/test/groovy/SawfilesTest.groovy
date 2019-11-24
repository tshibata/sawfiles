import spock.lang.Specification
import java.nio.file.Files

class SawfilesTest extends Specification {
	def "modify a tar file"() {
        setup:
		["rm", "-f", "build/tmp/a.tar"].execute().waitFor()
		["tar", "cf", "build/tmp/a.tar", "src/test/resources/hello.txt"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/tar.dsl")

		then:
		["tar", "tf", "build/tmp/a.tar"].execute().text.contains("newfile")
	}
	def "modify a zip file"() {
        setup:
		["rm", "-f", "build/tmp/a.zip"].execute().waitFor()
		["zip", "build/tmp/a.zip", "src/test/resources/hello.txt"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/zip.dsl")

		then:
		["unzip", "-l", "build/tmp/a.zip"].execute().text.contains("newfile")
	}
	def "modify a zip in a tar"() {
        setup:
		["rm", "-f", "build/tmp/a.zip"].execute().waitFor()
		["zip", "build/tmp/a.zip", "src/test/resources/hello.txt"].execute().waitFor()
		["rm", "-f", "build/tmp/nested.tar"].execute().waitFor()
		["tar", "cf", "build/tmp/nested.tar", "build/tmp/a.zip"].execute().waitFor()

		when:
		Sawfiles.main("src/test/resources/nested.dsl")

		["tar", "xfC", "build/tmp/nested.tar", "build/tmp"].execute().waitFor()

		then:
		["unzip", "-l", "build/tmp/build/tmp/a.zip"].execute().text.contains("newfile")
	}
}
