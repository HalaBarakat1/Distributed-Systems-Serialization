# Protocol Buffers part of Lab 3

This is separate from the existing httpserver/httpclient and from Java Serialization.

- `customer_profile.proto`: original proto2 schema (sender)
- `schema-evolution/renamed_customer_profile.proto`: receiver-only schema with `client_full_name = 1`
- `src/main/java/com/distributed/lab/CustomerProfileProto.java`: **real protoc-generated Java source** (protoc 3.13.0)
- `src/main/java/com/distributed/lab/RenamedCustomerProfileProto.java`: **real protoc-generated Java source** (protoc 3.13.0)
- `src/main/java/ProtobufLab.java`: official Google Java protobuf Builder/parseFrom demo
- `src/main/java/ProtobufWireLab.java`: dependency-free byte-level verifier, not an official protobuf library substitute
- `pom.xml`: protobuf-java version 3.13.0

On Windows PowerShell with Maven installed:

```powershell
mvn -q clean package
mvn -q exec:java "-Dexec.mainClass=ProtobufLab"
mvn -q exec:java "-Dexec.mainClass=ProtobufLab" "-Dexec.args=evolution"
```

To regenerate the stubs if protoc is installed:

```powershell
protoc --java_out=src/main/java customer_profile.proto
protoc --proto_path=schema-evolution --java_out=src/main/java renamed_customer_profile.proto
```

The two proto files deliberately share their message full name and are compiled separately as sender and receiver versions. Run from `protobuf-lab`.
