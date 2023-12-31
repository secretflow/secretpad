// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: secretflow/spec/v1/report.proto

package com.secretflow.spec.v1;

public interface TableOrBuilder extends
    // @@protoc_insertion_point(interface_extends:secretflow.spec.v1.Table)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Name of the Table.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <pre>
   * Name of the Table.
   * </pre>
   *
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string desc = 2;</code>
   * @return The desc.
   */
  java.lang.String getDesc();
  /**
   * <code>string desc = 2;</code>
   * @return The bytes for desc.
   */
  com.google.protobuf.ByteString
      getDescBytes();

  /**
   * <code>repeated .secretflow.spec.v1.Table.HeaderItem headers = 3;</code>
   */
  java.util.List<com.secretflow.spec.v1.Table.HeaderItem> 
      getHeadersList();
  /**
   * <code>repeated .secretflow.spec.v1.Table.HeaderItem headers = 3;</code>
   */
  com.secretflow.spec.v1.Table.HeaderItem getHeaders(int index);
  /**
   * <code>repeated .secretflow.spec.v1.Table.HeaderItem headers = 3;</code>
   */
  int getHeadersCount();
  /**
   * <code>repeated .secretflow.spec.v1.Table.HeaderItem headers = 3;</code>
   */
  java.util.List<? extends com.secretflow.spec.v1.Table.HeaderItemOrBuilder> 
      getHeadersOrBuilderList();
  /**
   * <code>repeated .secretflow.spec.v1.Table.HeaderItem headers = 3;</code>
   */
  com.secretflow.spec.v1.Table.HeaderItemOrBuilder getHeadersOrBuilder(
      int index);

  /**
   * <code>repeated .secretflow.spec.v1.Table.Row rows = 4;</code>
   */
  java.util.List<com.secretflow.spec.v1.Table.Row> 
      getRowsList();
  /**
   * <code>repeated .secretflow.spec.v1.Table.Row rows = 4;</code>
   */
  com.secretflow.spec.v1.Table.Row getRows(int index);
  /**
   * <code>repeated .secretflow.spec.v1.Table.Row rows = 4;</code>
   */
  int getRowsCount();
  /**
   * <code>repeated .secretflow.spec.v1.Table.Row rows = 4;</code>
   */
  java.util.List<? extends com.secretflow.spec.v1.Table.RowOrBuilder> 
      getRowsOrBuilderList();
  /**
   * <code>repeated .secretflow.spec.v1.Table.Row rows = 4;</code>
   */
  com.secretflow.spec.v1.Table.RowOrBuilder getRowsOrBuilder(
      int index);
}
