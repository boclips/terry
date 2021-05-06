import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.31"
	kotlin("plugin.spring") version "1.4.31"
}

group = "com.boclips"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("io.github.microutils:kotlin-logging:1.6.25")
	implementation("com.github.boclips:kaltura-client:0.58.0")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.1")
	implementation("com.github.boclips:videos:v751")
	implementation("io.github.openfeign:feign-core:10.7.2")
	implementation("io.github.openfeign:feign-okhttp:11.0")
	implementation("io.opentracing.contrib:opentracing-spring-jaeger-cloud-starter:3.2.2")
	implementation("org.springframework.cloud:spring-cloud-gcp-starter-storage:1.2.8.RELEASE")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
