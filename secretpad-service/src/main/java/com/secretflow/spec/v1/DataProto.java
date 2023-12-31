// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: secretflow/spec/v1/data.proto

package com.secretflow.spec.v1;

public final class DataProto {
  private DataProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_SystemInfo_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_SystemInfo_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_StorageConfig_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_StorageConfig_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_StorageConfig_LocalFSConfig_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_StorageConfig_LocalFSConfig_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_DistData_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_DistData_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_DistData_DataRef_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_DistData_DataRef_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_VerticalTable_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_VerticalTable_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_IndividualTable_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_IndividualTable_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_secretflow_spec_v1_TableSchema_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_secretflow_spec_v1_TableSchema_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\035secretflow/spec/v1/data.proto\022\022secretf" +
      "low.spec.v1\032\031google/protobuf/any.proto\"A" +
      "\n\nSystemInfo\022\013\n\003app\030\001 \001(\t\022&\n\010app_meta\030\002 " +
      "\001(\0132\024.google.protobuf.Any\"}\n\rStorageConf" +
      "ig\022\014\n\004type\030\001 \001(\t\022A\n\010local_fs\030\002 \001(\0132/.sec" +
      "retflow.spec.v1.StorageConfig.LocalFSCon" +
      "fig\032\033\n\rLocalFSConfig\022\n\n\002wd\030\001 \001(\t\"\357\001\n\010Dis" +
      "tData\022\014\n\004name\030\001 \001(\t\022\014\n\004type\030\002 \001(\t\0223\n\013sys" +
      "tem_info\030\003 \001(\0132\036.secretflow.spec.v1.Syst" +
      "emInfo\022\"\n\004meta\030\004 \001(\0132\024.google.protobuf.A" +
      "ny\0227\n\tdata_refs\030\005 \003(\0132$.secretflow.spec." +
      "v1.DistData.DataRef\0325\n\007DataRef\022\013\n\003uri\030\001 " +
      "\001(\t\022\r\n\005party\030\002 \001(\t\022\016\n\006format\030\003 \001(\t\"U\n\rVe" +
      "rticalTable\0220\n\007schemas\030\001 \003(\0132\037.secretflo" +
      "w.spec.v1.TableSchema\022\022\n\nline_count\030\002 \001(" +
      "\003\"V\n\017IndividualTable\022/\n\006schema\030\001 \001(\0132\037.s" +
      "ecretflow.spec.v1.TableSchema\022\022\n\nline_co" +
      "unt\030\002 \001(\003\"z\n\013TableSchema\022\013\n\003ids\030\001 \003(\t\022\020\n" +
      "\010features\030\002 \003(\t\022\016\n\006labels\030\003 \003(\t\022\020\n\010id_ty" +
      "pes\030\004 \003(\t\022\025\n\rfeature_types\030\005 \003(\t\022\023\n\013labe" +
      "l_types\030\006 \003(\tB%\n\026com.secretflow.spec.v1B" +
      "\tDataProtoP\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.AnyProto.getDescriptor(),
        });
    internal_static_secretflow_spec_v1_SystemInfo_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_secretflow_spec_v1_SystemInfo_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_SystemInfo_descriptor,
        new java.lang.String[] { "App", "AppMeta", });
    internal_static_secretflow_spec_v1_StorageConfig_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_secretflow_spec_v1_StorageConfig_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_StorageConfig_descriptor,
        new java.lang.String[] { "Type", "LocalFs", });
    internal_static_secretflow_spec_v1_StorageConfig_LocalFSConfig_descriptor =
      internal_static_secretflow_spec_v1_StorageConfig_descriptor.getNestedTypes().get(0);
    internal_static_secretflow_spec_v1_StorageConfig_LocalFSConfig_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_StorageConfig_LocalFSConfig_descriptor,
        new java.lang.String[] { "Wd", });
    internal_static_secretflow_spec_v1_DistData_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_secretflow_spec_v1_DistData_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_DistData_descriptor,
        new java.lang.String[] { "Name", "Type", "SystemInfo", "Meta", "DataRefs", });
    internal_static_secretflow_spec_v1_DistData_DataRef_descriptor =
      internal_static_secretflow_spec_v1_DistData_descriptor.getNestedTypes().get(0);
    internal_static_secretflow_spec_v1_DistData_DataRef_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_DistData_DataRef_descriptor,
        new java.lang.String[] { "Uri", "Party", "Format", });
    internal_static_secretflow_spec_v1_VerticalTable_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_secretflow_spec_v1_VerticalTable_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_VerticalTable_descriptor,
        new java.lang.String[] { "Schemas", "LineCount", });
    internal_static_secretflow_spec_v1_IndividualTable_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_secretflow_spec_v1_IndividualTable_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_IndividualTable_descriptor,
        new java.lang.String[] { "Schema", "LineCount", });
    internal_static_secretflow_spec_v1_TableSchema_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_secretflow_spec_v1_TableSchema_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_secretflow_spec_v1_TableSchema_descriptor,
        new java.lang.String[] { "Ids", "Features", "Labels", "IdTypes", "FeatureTypes", "LabelTypes", });
    com.google.protobuf.AnyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
