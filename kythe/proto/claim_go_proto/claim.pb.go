// Code generated by protoc-gen-go. DO NOT EDIT.
// source: kythe/proto/claim.proto

package claim_go_proto

import proto "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"
import storage_go_proto "kythe.io/kythe/proto/storage_go_proto"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion2 // please upgrade the proto package

type ClaimAssignment struct {
	CompilationVName     *storage_go_proto.VName `protobuf:"bytes,1,opt,name=compilation_v_name,json=compilationVName" json:"compilation_v_name,omitempty"`
	DependencyVName      *storage_go_proto.VName `protobuf:"bytes,2,opt,name=dependency_v_name,json=dependencyVName" json:"dependency_v_name,omitempty"`
	XXX_NoUnkeyedLiteral struct{}                `json:"-"`
	XXX_unrecognized     []byte                  `json:"-"`
	XXX_sizecache        int32                   `json:"-"`
}

func (m *ClaimAssignment) Reset()         { *m = ClaimAssignment{} }
func (m *ClaimAssignment) String() string { return proto.CompactTextString(m) }
func (*ClaimAssignment) ProtoMessage()    {}
func (*ClaimAssignment) Descriptor() ([]byte, []int) {
	return fileDescriptor_claim_3e8aaaead8b4272e, []int{0}
}
func (m *ClaimAssignment) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_ClaimAssignment.Unmarshal(m, b)
}
func (m *ClaimAssignment) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_ClaimAssignment.Marshal(b, m, deterministic)
}
func (dst *ClaimAssignment) XXX_Merge(src proto.Message) {
	xxx_messageInfo_ClaimAssignment.Merge(dst, src)
}
func (m *ClaimAssignment) XXX_Size() int {
	return xxx_messageInfo_ClaimAssignment.Size(m)
}
func (m *ClaimAssignment) XXX_DiscardUnknown() {
	xxx_messageInfo_ClaimAssignment.DiscardUnknown(m)
}

var xxx_messageInfo_ClaimAssignment proto.InternalMessageInfo

func (m *ClaimAssignment) GetCompilationVName() *storage_go_proto.VName {
	if m != nil {
		return m.CompilationVName
	}
	return nil
}

func (m *ClaimAssignment) GetDependencyVName() *storage_go_proto.VName {
	if m != nil {
		return m.DependencyVName
	}
	return nil
}

func init() {
	proto.RegisterType((*ClaimAssignment)(nil), "kythe.proto.ClaimAssignment")
}

func init() { proto.RegisterFile("kythe/proto/claim.proto", fileDescriptor_claim_3e8aaaead8b4272e) }

var fileDescriptor_claim_3e8aaaead8b4272e = []byte{
	// 181 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0xe2, 0x12, 0xcf, 0xae, 0x2c, 0xc9,
	0x48, 0xd5, 0x2f, 0x28, 0xca, 0x2f, 0xc9, 0xd7, 0x4f, 0xce, 0x49, 0xcc, 0xcc, 0xd5, 0x03, 0xb3,
	0x85, 0xb8, 0xc1, 0x12, 0x10, 0x8e, 0x94, 0x24, 0xb2, 0xaa, 0xe2, 0x92, 0xfc, 0xa2, 0xc4, 0x74,
	0xa8, 0x94, 0xd2, 0x64, 0x46, 0x2e, 0x7e, 0x67, 0x90, 0x3e, 0xc7, 0xe2, 0xe2, 0xcc, 0xf4, 0xbc,
	0xdc, 0xd4, 0xbc, 0x12, 0x21, 0x07, 0x2e, 0xa1, 0xe4, 0xfc, 0xdc, 0x82, 0xcc, 0x9c, 0xc4, 0x92,
	0xcc, 0xfc, 0xbc, 0xf8, 0xb2, 0xf8, 0xbc, 0xc4, 0xdc, 0x54, 0x09, 0x46, 0x05, 0x46, 0x0d, 0x6e,
	0x23, 0x21, 0x3d, 0x24, 0x83, 0xf5, 0xc2, 0xfc, 0x12, 0x73, 0x53, 0x83, 0x04, 0x90, 0x54, 0x83,
	0x45, 0x84, 0xec, 0xb8, 0x04, 0x53, 0x52, 0x0b, 0x52, 0xf3, 0x52, 0x52, 0xf3, 0x92, 0x2b, 0x61,
	0x06, 0x30, 0xe1, 0x34, 0x80, 0x1f, 0xa1, 0x18, 0x2c, 0xe0, 0xa4, 0xc8, 0x25, 0x9f, 0x9c, 0x9f,
	0xab, 0x97, 0x9e, 0x9f, 0x9f, 0x9e, 0x93, 0xaa, 0x97, 0x92, 0x5a, 0x56, 0x92, 0x9f, 0x9f, 0x53,
	0x8c, 0xac, 0x33, 0x89, 0x0d, 0x4c, 0x19, 0x03, 0x02, 0x00, 0x00, 0xff, 0xff, 0xe0, 0xf9, 0xe7,
	0xf0, 0x02, 0x01, 0x00, 0x00,
}
