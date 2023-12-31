// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: secretflow/spec/v1/evaluation.proto

package com.secretflow.spec.v1;

public final class EvaluationProto {
  private EvaluationProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_NodeEvalParam_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_NodeEvalParam_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_NodeEvalResult_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_NodeEvalResult_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n#secretflow/spec/v1/evaluation.proto\022\022s" +
      "ecretflow.spec.v1\032\"secretflow/spec/v1/co" +
      "mponent.proto\032\035secretflow/spec/v1/data.p" +
      "roto\"\303\001\n\rNodeEvalParam\022\016\n\006domain\030\001 \001(\t\022\014" +
      "\n\004name\030\002 \001(\t\022\017\n\007version\030\003 \001(\t\022\022\n\nattr_pa" +
      "ths\030\004 \003(\t\022,\n\005attrs\030\005 \003(\0132\035.secretflow.sp" +
      "ec.v1.Attribute\022,\n\006inputs\030\006 \003(\0132\034.secret" +
      "flow.spec.v1.DistData\022\023\n\013output_uris\030\007 \003" +
      "(\t\"?\n\016NodeEvalResult\022-\n\007outputs\030\001 \003(\0132\034." +
      "secretflow.spec.v1.DistDataB+\n\026com.secre" +
      "tflow.spec.v1B\017EvaluationProtoP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.secretflow.spec.v1.ComponentProto.getDescriptor(),
          com.secretflow.spec.v1.DataProto.getDescriptor(),
        });
    internal_static_secretflow_spec_v1_NodeEvalParam_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_secretflow_spec_v1_NodeEvalParam_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_NodeEvalParam_descriptor,
        new java.lang.String[] { "Domain", "Name", "Version", "AttrPaths", "Attrs", "Inputs", "OutputUris", });
    internal_static_secretflow_spec_v1_NodeEvalResult_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_secretflow_spec_v1_NodeEvalResult_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_NodeEvalResult_descriptor,
        new java.lang.String[] { "Outputs", });
    com.secretflow.spec.v1.ComponentProto.getDescriptor();
    com.secretflow.spec.v1.DataProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
