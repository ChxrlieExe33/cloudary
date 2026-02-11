resource "aws_vpc" "cloudary_vpc" {

  cidr_block = "10.0.0.0/16"
  enable_dns_support = true
  enable_dns_hostnames = true

  tags = {
    Name = "cloudary-vpc"
  }

}

resource "aws_internet_gateway" "cloudary_igw" {
  vpc_id = aws_vpc.cloudary_vpc.id

  tags = {
    Name = "cloudary-igw"
  }
}