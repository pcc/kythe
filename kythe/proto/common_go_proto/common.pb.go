// Code generated by protoc-gen-go. DO NOT EDIT.
// source: kythe/proto/common.proto

package common_go_proto

import proto "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion2 // please upgrade the proto package

type Link_Kind int32

const (
	Link_DEFINITION Link_Kind = 0
	Link_LIST       Link_Kind = 1
	Link_LIST_ITEM  Link_Kind = 2
	Link_IMPORTANT  Link_Kind = 999
)

var Link_Kind_name = map[int32]string{
	0:   "DEFINITION",
	1:   "LIST",
	2:   "LIST_ITEM",
	999: "IMPORTANT",
}
var Link_Kind_value = map[string]int32{
	"DEFINITION": 0,
	"LIST":       1,
	"LIST_ITEM":  2,
	"IMPORTANT":  999,
}

func (x Link_Kind) String() string {
	return proto.EnumName(Link_Kind_name, int32(x))
}
func (Link_Kind) EnumDescriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{5, 0}
}

type MarkedSource_Kind int32

const (
	MarkedSource_BOX                                     MarkedSource_Kind = 0
	MarkedSource_TYPE                                    MarkedSource_Kind = 1
	MarkedSource_PARAMETER                               MarkedSource_Kind = 2
	MarkedSource_IDENTIFIER                              MarkedSource_Kind = 3
	MarkedSource_CONTEXT                                 MarkedSource_Kind = 4
	MarkedSource_INITIALIZER                             MarkedSource_Kind = 5
	MarkedSource_PARAMETER_LOOKUP_BY_PARAM               MarkedSource_Kind = 6
	MarkedSource_LOOKUP_BY_PARAM                         MarkedSource_Kind = 7
	MarkedSource_PARAMETER_LOOKUP_BY_PARAM_WITH_DEFAULTS MarkedSource_Kind = 8
)

var MarkedSource_Kind_name = map[int32]string{
	0: "BOX",
	1: "TYPE",
	2: "PARAMETER",
	3: "IDENTIFIER",
	4: "CONTEXT",
	5: "INITIALIZER",
	6: "PARAMETER_LOOKUP_BY_PARAM",
	7: "LOOKUP_BY_PARAM",
	8: "PARAMETER_LOOKUP_BY_PARAM_WITH_DEFAULTS",
}
var MarkedSource_Kind_value = map[string]int32{
	"BOX":                                     0,
	"TYPE":                                    1,
	"PARAMETER":                               2,
	"IDENTIFIER":                              3,
	"CONTEXT":                                 4,
	"INITIALIZER":                             5,
	"PARAMETER_LOOKUP_BY_PARAM":               6,
	"LOOKUP_BY_PARAM":                         7,
	"PARAMETER_LOOKUP_BY_PARAM_WITH_DEFAULTS": 8,
}

func (x MarkedSource_Kind) String() string {
	return proto.EnumName(MarkedSource_Kind_name, int32(x))
}
func (MarkedSource_Kind) EnumDescriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{6, 0}
}

type Fact struct {
	Name                 string   `protobuf:"bytes,1,opt,name=name" json:"name,omitempty"`
	Value                []byte   `protobuf:"bytes,2,opt,name=value,proto3" json:"value,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *Fact) Reset()         { *m = Fact{} }
func (m *Fact) String() string { return proto.CompactTextString(m) }
func (*Fact) ProtoMessage()    {}
func (*Fact) Descriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{0}
}
func (m *Fact) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_Fact.Unmarshal(m, b)
}
func (m *Fact) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_Fact.Marshal(b, m, deterministic)
}
func (dst *Fact) XXX_Merge(src proto.Message) {
	xxx_messageInfo_Fact.Merge(dst, src)
}
func (m *Fact) XXX_Size() int {
	return xxx_messageInfo_Fact.Size(m)
}
func (m *Fact) XXX_DiscardUnknown() {
	xxx_messageInfo_Fact.DiscardUnknown(m)
}

var xxx_messageInfo_Fact proto.InternalMessageInfo

func (m *Fact) GetName() string {
	if m != nil {
		return m.Name
	}
	return ""
}

func (m *Fact) GetValue() []byte {
	if m != nil {
		return m.Value
	}
	return nil
}

type Point struct {
	ByteOffset           int32    `protobuf:"varint,1,opt,name=byte_offset,json=byteOffset" json:"byte_offset,omitempty"`
	LineNumber           int32    `protobuf:"varint,2,opt,name=line_number,json=lineNumber" json:"line_number,omitempty"`
	ColumnOffset         int32    `protobuf:"varint,3,opt,name=column_offset,json=columnOffset" json:"column_offset,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *Point) Reset()         { *m = Point{} }
func (m *Point) String() string { return proto.CompactTextString(m) }
func (*Point) ProtoMessage()    {}
func (*Point) Descriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{1}
}
func (m *Point) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_Point.Unmarshal(m, b)
}
func (m *Point) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_Point.Marshal(b, m, deterministic)
}
func (dst *Point) XXX_Merge(src proto.Message) {
	xxx_messageInfo_Point.Merge(dst, src)
}
func (m *Point) XXX_Size() int {
	return xxx_messageInfo_Point.Size(m)
}
func (m *Point) XXX_DiscardUnknown() {
	xxx_messageInfo_Point.DiscardUnknown(m)
}

var xxx_messageInfo_Point proto.InternalMessageInfo

func (m *Point) GetByteOffset() int32 {
	if m != nil {
		return m.ByteOffset
	}
	return 0
}

func (m *Point) GetLineNumber() int32 {
	if m != nil {
		return m.LineNumber
	}
	return 0
}

func (m *Point) GetColumnOffset() int32 {
	if m != nil {
		return m.ColumnOffset
	}
	return 0
}

type Span struct {
	Start                *Point   `protobuf:"bytes,1,opt,name=start" json:"start,omitempty"`
	End                  *Point   `protobuf:"bytes,2,opt,name=end" json:"end,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *Span) Reset()         { *m = Span{} }
func (m *Span) String() string { return proto.CompactTextString(m) }
func (*Span) ProtoMessage()    {}
func (*Span) Descriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{2}
}
func (m *Span) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_Span.Unmarshal(m, b)
}
func (m *Span) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_Span.Marshal(b, m, deterministic)
}
func (dst *Span) XXX_Merge(src proto.Message) {
	xxx_messageInfo_Span.Merge(dst, src)
}
func (m *Span) XXX_Size() int {
	return xxx_messageInfo_Span.Size(m)
}
func (m *Span) XXX_DiscardUnknown() {
	xxx_messageInfo_Span.DiscardUnknown(m)
}

var xxx_messageInfo_Span proto.InternalMessageInfo

func (m *Span) GetStart() *Point {
	if m != nil {
		return m.Start
	}
	return nil
}

func (m *Span) GetEnd() *Point {
	if m != nil {
		return m.End
	}
	return nil
}

type NodeInfo struct {
	Facts                map[string][]byte `protobuf:"bytes,2,rep,name=facts" json:"facts,omitempty" protobuf_key:"bytes,1,opt,name=key" protobuf_val:"bytes,2,opt,name=value,proto3"`
	Definition           string            `protobuf:"bytes,5,opt,name=definition" json:"definition,omitempty"`
	XXX_NoUnkeyedLiteral struct{}          `json:"-"`
	XXX_unrecognized     []byte            `json:"-"`
	XXX_sizecache        int32             `json:"-"`
}

func (m *NodeInfo) Reset()         { *m = NodeInfo{} }
func (m *NodeInfo) String() string { return proto.CompactTextString(m) }
func (*NodeInfo) ProtoMessage()    {}
func (*NodeInfo) Descriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{3}
}
func (m *NodeInfo) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_NodeInfo.Unmarshal(m, b)
}
func (m *NodeInfo) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_NodeInfo.Marshal(b, m, deterministic)
}
func (dst *NodeInfo) XXX_Merge(src proto.Message) {
	xxx_messageInfo_NodeInfo.Merge(dst, src)
}
func (m *NodeInfo) XXX_Size() int {
	return xxx_messageInfo_NodeInfo.Size(m)
}
func (m *NodeInfo) XXX_DiscardUnknown() {
	xxx_messageInfo_NodeInfo.DiscardUnknown(m)
}

var xxx_messageInfo_NodeInfo proto.InternalMessageInfo

func (m *NodeInfo) GetFacts() map[string][]byte {
	if m != nil {
		return m.Facts
	}
	return nil
}

func (m *NodeInfo) GetDefinition() string {
	if m != nil {
		return m.Definition
	}
	return ""
}

type Diagnostic struct {
	Span                 *Span    `protobuf:"bytes,1,opt,name=span" json:"span,omitempty"`
	Message              string   `protobuf:"bytes,2,opt,name=message" json:"message,omitempty"`
	Details              string   `protobuf:"bytes,3,opt,name=details" json:"details,omitempty"`
	ContextUrl           string   `protobuf:"bytes,4,opt,name=context_url,json=contextUrl" json:"context_url,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *Diagnostic) Reset()         { *m = Diagnostic{} }
func (m *Diagnostic) String() string { return proto.CompactTextString(m) }
func (*Diagnostic) ProtoMessage()    {}
func (*Diagnostic) Descriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{4}
}
func (m *Diagnostic) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_Diagnostic.Unmarshal(m, b)
}
func (m *Diagnostic) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_Diagnostic.Marshal(b, m, deterministic)
}
func (dst *Diagnostic) XXX_Merge(src proto.Message) {
	xxx_messageInfo_Diagnostic.Merge(dst, src)
}
func (m *Diagnostic) XXX_Size() int {
	return xxx_messageInfo_Diagnostic.Size(m)
}
func (m *Diagnostic) XXX_DiscardUnknown() {
	xxx_messageInfo_Diagnostic.DiscardUnknown(m)
}

var xxx_messageInfo_Diagnostic proto.InternalMessageInfo

func (m *Diagnostic) GetSpan() *Span {
	if m != nil {
		return m.Span
	}
	return nil
}

func (m *Diagnostic) GetMessage() string {
	if m != nil {
		return m.Message
	}
	return ""
}

func (m *Diagnostic) GetDetails() string {
	if m != nil {
		return m.Details
	}
	return ""
}

func (m *Diagnostic) GetContextUrl() string {
	if m != nil {
		return m.ContextUrl
	}
	return ""
}

type Link struct {
	Definition           []string  `protobuf:"bytes,3,rep,name=definition" json:"definition,omitempty"`
	Kind                 Link_Kind `protobuf:"varint,2,opt,name=kind,enum=kythe.proto.common.Link_Kind" json:"kind,omitempty"` // Deprecated: Do not use.
	XXX_NoUnkeyedLiteral struct{}  `json:"-"`
	XXX_unrecognized     []byte    `json:"-"`
	XXX_sizecache        int32     `json:"-"`
}

func (m *Link) Reset()         { *m = Link{} }
func (m *Link) String() string { return proto.CompactTextString(m) }
func (*Link) ProtoMessage()    {}
func (*Link) Descriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{5}
}
func (m *Link) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_Link.Unmarshal(m, b)
}
func (m *Link) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_Link.Marshal(b, m, deterministic)
}
func (dst *Link) XXX_Merge(src proto.Message) {
	xxx_messageInfo_Link.Merge(dst, src)
}
func (m *Link) XXX_Size() int {
	return xxx_messageInfo_Link.Size(m)
}
func (m *Link) XXX_DiscardUnknown() {
	xxx_messageInfo_Link.DiscardUnknown(m)
}

var xxx_messageInfo_Link proto.InternalMessageInfo

func (m *Link) GetDefinition() []string {
	if m != nil {
		return m.Definition
	}
	return nil
}

// Deprecated: Do not use.
func (m *Link) GetKind() Link_Kind {
	if m != nil {
		return m.Kind
	}
	return Link_DEFINITION
}

type MarkedSource struct {
	Kind                 MarkedSource_Kind `protobuf:"varint,1,opt,name=kind,enum=kythe.proto.common.MarkedSource_Kind" json:"kind,omitempty"`
	PreText              string            `protobuf:"bytes,2,opt,name=pre_text,json=preText" json:"pre_text,omitempty"`
	Child                []*MarkedSource   `protobuf:"bytes,3,rep,name=child" json:"child,omitempty"`
	PostChildText        string            `protobuf:"bytes,4,opt,name=post_child_text,json=postChildText" json:"post_child_text,omitempty"`
	PostText             string            `protobuf:"bytes,5,opt,name=post_text,json=postText" json:"post_text,omitempty"`
	LookupIndex          uint32            `protobuf:"varint,6,opt,name=lookup_index,json=lookupIndex" json:"lookup_index,omitempty"`
	DefaultChildrenCount uint32            `protobuf:"varint,7,opt,name=default_children_count,json=defaultChildrenCount" json:"default_children_count,omitempty"`
	AddFinalListToken    bool              `protobuf:"varint,10,opt,name=add_final_list_token,json=addFinalListToken" json:"add_final_list_token,omitempty"`
	Link                 []*Link           `protobuf:"bytes,11,rep,name=link" json:"link,omitempty"`
	XXX_NoUnkeyedLiteral struct{}          `json:"-"`
	XXX_unrecognized     []byte            `json:"-"`
	XXX_sizecache        int32             `json:"-"`
}

func (m *MarkedSource) Reset()         { *m = MarkedSource{} }
func (m *MarkedSource) String() string { return proto.CompactTextString(m) }
func (*MarkedSource) ProtoMessage()    {}
func (*MarkedSource) Descriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{6}
}
func (m *MarkedSource) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_MarkedSource.Unmarshal(m, b)
}
func (m *MarkedSource) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_MarkedSource.Marshal(b, m, deterministic)
}
func (dst *MarkedSource) XXX_Merge(src proto.Message) {
	xxx_messageInfo_MarkedSource.Merge(dst, src)
}
func (m *MarkedSource) XXX_Size() int {
	return xxx_messageInfo_MarkedSource.Size(m)
}
func (m *MarkedSource) XXX_DiscardUnknown() {
	xxx_messageInfo_MarkedSource.DiscardUnknown(m)
}

var xxx_messageInfo_MarkedSource proto.InternalMessageInfo

func (m *MarkedSource) GetKind() MarkedSource_Kind {
	if m != nil {
		return m.Kind
	}
	return MarkedSource_BOX
}

func (m *MarkedSource) GetPreText() string {
	if m != nil {
		return m.PreText
	}
	return ""
}

func (m *MarkedSource) GetChild() []*MarkedSource {
	if m != nil {
		return m.Child
	}
	return nil
}

func (m *MarkedSource) GetPostChildText() string {
	if m != nil {
		return m.PostChildText
	}
	return ""
}

func (m *MarkedSource) GetPostText() string {
	if m != nil {
		return m.PostText
	}
	return ""
}

func (m *MarkedSource) GetLookupIndex() uint32 {
	if m != nil {
		return m.LookupIndex
	}
	return 0
}

func (m *MarkedSource) GetDefaultChildrenCount() uint32 {
	if m != nil {
		return m.DefaultChildrenCount
	}
	return 0
}

func (m *MarkedSource) GetAddFinalListToken() bool {
	if m != nil {
		return m.AddFinalListToken
	}
	return false
}

func (m *MarkedSource) GetLink() []*Link {
	if m != nil {
		return m.Link
	}
	return nil
}

type SymbolInfo struct {
	BaseName             string   `protobuf:"bytes,1,opt,name=base_name,json=baseName" json:"base_name,omitempty"`
	QualifiedName        string   `protobuf:"bytes,2,opt,name=qualified_name,json=qualifiedName" json:"qualified_name,omitempty"`
	XXX_NoUnkeyedLiteral struct{} `json:"-"`
	XXX_unrecognized     []byte   `json:"-"`
	XXX_sizecache        int32    `json:"-"`
}

func (m *SymbolInfo) Reset()         { *m = SymbolInfo{} }
func (m *SymbolInfo) String() string { return proto.CompactTextString(m) }
func (*SymbolInfo) ProtoMessage()    {}
func (*SymbolInfo) Descriptor() ([]byte, []int) {
	return fileDescriptor_common_877e1185c28f4bae, []int{7}
}
func (m *SymbolInfo) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_SymbolInfo.Unmarshal(m, b)
}
func (m *SymbolInfo) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_SymbolInfo.Marshal(b, m, deterministic)
}
func (dst *SymbolInfo) XXX_Merge(src proto.Message) {
	xxx_messageInfo_SymbolInfo.Merge(dst, src)
}
func (m *SymbolInfo) XXX_Size() int {
	return xxx_messageInfo_SymbolInfo.Size(m)
}
func (m *SymbolInfo) XXX_DiscardUnknown() {
	xxx_messageInfo_SymbolInfo.DiscardUnknown(m)
}

var xxx_messageInfo_SymbolInfo proto.InternalMessageInfo

func (m *SymbolInfo) GetBaseName() string {
	if m != nil {
		return m.BaseName
	}
	return ""
}

func (m *SymbolInfo) GetQualifiedName() string {
	if m != nil {
		return m.QualifiedName
	}
	return ""
}

func init() {
	proto.RegisterType((*Fact)(nil), "kythe.proto.common.Fact")
	proto.RegisterType((*Point)(nil), "kythe.proto.common.Point")
	proto.RegisterType((*Span)(nil), "kythe.proto.common.Span")
	proto.RegisterType((*NodeInfo)(nil), "kythe.proto.common.NodeInfo")
	proto.RegisterMapType((map[string][]byte)(nil), "kythe.proto.common.NodeInfo.FactsEntry")
	proto.RegisterType((*Diagnostic)(nil), "kythe.proto.common.Diagnostic")
	proto.RegisterType((*Link)(nil), "kythe.proto.common.Link")
	proto.RegisterType((*MarkedSource)(nil), "kythe.proto.common.MarkedSource")
	proto.RegisterType((*SymbolInfo)(nil), "kythe.proto.common.SymbolInfo")
	proto.RegisterEnum("kythe.proto.common.Link_Kind", Link_Kind_name, Link_Kind_value)
	proto.RegisterEnum("kythe.proto.common.MarkedSource_Kind", MarkedSource_Kind_name, MarkedSource_Kind_value)
}

func init() { proto.RegisterFile("kythe/proto/common.proto", fileDescriptor_common_877e1185c28f4bae) }

var fileDescriptor_common_877e1185c28f4bae = []byte{
	// 848 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0x84, 0x54, 0xed, 0x8e, 0xdb, 0x44,
	0x14, 0xc5, 0x89, 0xbd, 0x49, 0x6e, 0xf6, 0xc3, 0x0c, 0x2b, 0xe4, 0xa5, 0x2a, 0x0d, 0xae, 0x4a,
	0x23, 0x15, 0x65, 0xd1, 0xf2, 0xa1, 0x82, 0x84, 0x50, 0x76, 0xd7, 0x11, 0xa6, 0xd9, 0x24, 0x9a,
	0x78, 0x45, 0xcb, 0x1f, 0x6b, 0x62, 0x4f, 0xb6, 0x23, 0x3b, 0x33, 0xc1, 0x1e, 0x57, 0x9b, 0xa7,
	0xe0, 0x25, 0x78, 0x01, 0x7e, 0xf1, 0x20, 0x3c, 0x04, 0xaf, 0x81, 0x66, 0xc6, 0x29, 0x4b, 0x49,
	0xe9, 0xbf, 0xb9, 0xe7, 0x9c, 0xfb, 0x31, 0xe7, 0x8e, 0x06, 0xbc, 0x6c, 0x23, 0x5f, 0xd2, 0xd3,
	0x75, 0x21, 0xa4, 0x38, 0x4d, 0xc4, 0x6a, 0x25, 0xf8, 0x40, 0x07, 0x08, 0x69, 0xc6, 0x04, 0x03,
	0xc3, 0xf8, 0x9f, 0x83, 0x3d, 0x22, 0x89, 0x44, 0x08, 0x6c, 0x4e, 0x56, 0xd4, 0xb3, 0x7a, 0x56,
	0xbf, 0x83, 0xf5, 0x19, 0x1d, 0x83, 0xf3, 0x8a, 0xe4, 0x15, 0xf5, 0x1a, 0x3d, 0xab, 0xbf, 0x8f,
	0x4d, 0xe0, 0x73, 0x70, 0x66, 0x82, 0x71, 0x89, 0x1e, 0x40, 0x77, 0xb1, 0x91, 0x34, 0x16, 0xcb,
	0x65, 0x49, 0xa5, 0xce, 0x74, 0x30, 0x28, 0x68, 0xaa, 0x11, 0x25, 0xc8, 0x19, 0xa7, 0x31, 0xaf,
	0x56, 0x0b, 0x5a, 0xe8, 0x2a, 0x0e, 0x06, 0x05, 0x4d, 0x34, 0x82, 0x1e, 0xc2, 0x41, 0x22, 0xf2,
	0x6a, 0xc5, 0xb7, 0x35, 0x9a, 0x5a, 0xb2, 0x6f, 0x40, 0x53, 0xc5, 0x4f, 0xc1, 0x9e, 0xaf, 0x09,
	0x47, 0xa7, 0xe0, 0x94, 0x92, 0x14, 0xa6, 0x51, 0xf7, 0xec, 0x64, 0xf0, 0xdf, 0xdb, 0x0c, 0xf4,
	0x60, 0xd8, 0xe8, 0xd0, 0x13, 0x68, 0x52, 0x9e, 0xea, 0xb6, 0xff, 0x2b, 0x57, 0x2a, 0xff, 0x77,
	0x0b, 0xda, 0x13, 0x91, 0xd2, 0x90, 0x2f, 0x05, 0xfa, 0x0e, 0x9c, 0x25, 0x49, 0x64, 0xe9, 0x35,
	0x7a, 0xcd, 0x7e, 0xf7, 0xec, 0xf1, 0xae, 0xdc, 0xad, 0x78, 0xa0, 0xec, 0x2b, 0x03, 0x2e, 0x8b,
	0x0d, 0x36, 0x59, 0xe8, 0x63, 0x80, 0x94, 0x2e, 0x19, 0x67, 0x92, 0x09, 0xee, 0x39, 0xda, 0xd1,
	0x3b, 0xc8, 0x47, 0x4f, 0x01, 0xfe, 0x49, 0x42, 0x2e, 0x34, 0x33, 0xba, 0xa9, 0x8d, 0x57, 0xc7,
	0xdd, 0xbe, 0x7f, 0xdb, 0x78, 0x6a, 0xfd, 0x68, 0xb7, 0x2d, 0xb7, 0x81, 0xf7, 0x24, 0x4b, 0x32,
	0x2a, 0xfd, 0x5f, 0x2d, 0x80, 0x4b, 0x46, 0x6e, 0xb8, 0x28, 0x25, 0x4b, 0xd0, 0x67, 0x60, 0x97,
	0x6b, 0xc2, 0x6b, 0x7f, 0xbc, 0x5d, 0x43, 0x2b, 0x23, 0xb1, 0x56, 0x21, 0x0f, 0x5a, 0x2b, 0x5a,
	0x96, 0xe4, 0xc6, 0xb4, 0xe9, 0xe0, 0x6d, 0xa8, 0x98, 0x94, 0x4a, 0xc2, 0xf2, 0x52, 0xef, 0xa3,
	0x83, 0xb7, 0xa1, 0x5a, 0x68, 0x22, 0xb8, 0xa4, 0xb7, 0x32, 0xae, 0x8a, 0xdc, 0xb3, 0xcd, 0xcd,
	0x6a, 0xe8, 0xba, 0xc8, 0xfd, 0xdf, 0x2c, 0xb0, 0xc7, 0x8c, 0x67, 0x6f, 0x58, 0xd0, 0xec, 0x35,
	0xff, 0x6d, 0x01, 0xfa, 0x0a, 0xec, 0x8c, 0xd5, 0xcb, 0x39, 0x3c, 0xbb, 0xbf, 0x6b, 0x56, 0x55,
	0x67, 0xf0, 0x8c, 0xf1, 0xf4, 0xbc, 0xe1, 0x59, 0x58, 0xcb, 0xfd, 0xef, 0xc1, 0x56, 0x08, 0x3a,
	0x04, 0xb8, 0x0c, 0x46, 0xe1, 0x24, 0x8c, 0xc2, 0xe9, 0xc4, 0x7d, 0x0f, 0xb5, 0xc1, 0x1e, 0x87,
	0xf3, 0xc8, 0xb5, 0xd0, 0x01, 0x74, 0xd4, 0x29, 0x0e, 0xa3, 0xe0, 0xca, 0x6d, 0xa0, 0x43, 0xe8,
	0x84, 0x57, 0xb3, 0x29, 0x8e, 0x86, 0x93, 0xc8, 0xfd, 0xab, 0x65, 0x0c, 0xf4, 0xff, 0xb4, 0x61,
	0xff, 0x8a, 0x14, 0x19, 0x4d, 0xe7, 0xa2, 0x2a, 0x12, 0x8a, 0xbe, 0xa9, 0xc7, 0xb1, 0xf4, 0x38,
	0x8f, 0x76, 0x8d, 0x73, 0x57, 0xaf, 0xc7, 0x32, 0x23, 0xa1, 0x13, 0x68, 0xaf, 0x0b, 0x1a, 0x2b,
	0x07, 0xb6, 0x46, 0xae, 0x0b, 0x1a, 0xd1, 0x5b, 0x89, 0xbe, 0x06, 0x27, 0x79, 0xc9, 0xf2, 0x54,
	0xdf, 0xbf, 0x7b, 0xd6, 0x7b, 0x57, 0x59, 0x6c, 0xe4, 0xe8, 0x53, 0x38, 0x5a, 0x8b, 0x52, 0xc6,
	0x3a, 0x32, 0x95, 0x8d, 0xd5, 0x07, 0x0a, 0xbe, 0x50, 0xa8, 0xae, 0x7f, 0x0f, 0x3a, 0x5a, 0xa7,
	0x15, 0xe6, 0x99, 0xb5, 0x15, 0xa0, 0xc9, 0x4f, 0x60, 0x3f, 0x17, 0x22, 0xab, 0xd6, 0x31, 0xe3,
	0x29, 0xbd, 0xf5, 0xf6, 0x7a, 0x56, 0xff, 0x00, 0x77, 0x0d, 0x16, 0x2a, 0x08, 0x7d, 0x09, 0x1f,
	0xa6, 0x74, 0x49, 0xaa, 0xbc, 0x6e, 0x55, 0x50, 0x1e, 0x27, 0xa2, 0xe2, 0xd2, 0x6b, 0x69, 0xf1,
	0x71, 0xcd, 0x5e, 0xd4, 0xe4, 0x85, 0xe2, 0xd0, 0x29, 0x1c, 0x93, 0x34, 0x8d, 0x97, 0x8c, 0x93,
	0x3c, 0xce, 0x99, 0xea, 0x2f, 0x32, 0xca, 0x3d, 0xe8, 0x59, 0xfd, 0x36, 0x7e, 0x9f, 0xa4, 0xe9,
	0x48, 0x51, 0x63, 0x56, 0xca, 0x48, 0x11, 0xea, 0x5d, 0xe6, 0x8c, 0x67, 0x5e, 0x57, 0xbb, 0xe0,
	0xbd, 0x6d, 0xd7, 0x58, 0xab, 0xfc, 0x3f, 0xac, 0x7a, 0xc7, 0x2d, 0x68, 0x9e, 0x4f, 0x9f, 0x9b,
	0xe5, 0x46, 0x2f, 0x66, 0x81, 0x59, 0xee, 0x6c, 0x88, 0x87, 0x57, 0x41, 0x14, 0x60, 0xbd, 0x5c,
	0x08, 0x2f, 0x83, 0x49, 0x14, 0x8e, 0xc2, 0x00, 0xbb, 0x4d, 0xd4, 0x85, 0xd6, 0xc5, 0x74, 0x12,
	0x05, 0xcf, 0x23, 0xd7, 0x46, 0x47, 0xd0, 0xd5, 0xef, 0x63, 0x38, 0x0e, 0x7f, 0x0e, 0xb0, 0xeb,
	0xa0, 0xfb, 0x70, 0xf2, 0x3a, 0x39, 0x1e, 0x4f, 0xa7, 0xcf, 0xae, 0x67, 0xf1, 0xf9, 0x8b, 0x58,
	0x63, 0xee, 0x1e, 0xfa, 0x00, 0x8e, 0xde, 0x04, 0x5b, 0xe8, 0x09, 0x3c, 0x7e, 0x6b, 0x4e, 0xfc,
	0x53, 0x18, 0xfd, 0x10, 0x5f, 0x06, 0xa3, 0xe1, 0xf5, 0x38, 0x9a, 0xbb, 0x6d, 0x7f, 0x06, 0x30,
	0xdf, 0xac, 0x16, 0x22, 0xd7, 0x7f, 0xc8, 0x3d, 0xe8, 0x2c, 0x48, 0x49, 0xe3, 0x3b, 0xbf, 0x6a,
	0x5b, 0x01, 0x13, 0xf5, 0xb3, 0x3e, 0x82, 0xc3, 0x5f, 0x2a, 0x92, 0xb3, 0x25, 0xa3, 0xa9, 0x51,
	0x98, 0xa7, 0x73, 0xf0, 0x1a, 0x55, 0xb2, 0xf3, 0x87, 0xf0, 0x20, 0x11, 0xab, 0xc1, 0x8d, 0x10,
	0x37, 0x39, 0x1d, 0xa4, 0xf4, 0x95, 0x14, 0x22, 0x2f, 0xef, 0x1a, 0x38, 0xb3, 0x16, 0x7b, 0xfa,
	0xf0, 0xc5, 0xdf, 0x01, 0x00, 0x00, 0xff, 0xff, 0x88, 0x18, 0xd7, 0x5c, 0xf8, 0x05, 0x00, 0x00,
}
