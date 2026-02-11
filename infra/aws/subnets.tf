resource "aws_subnet" "public_subnet_a" {

  vpc_id = aws_vpc.cloudary_vpc.id
  cidr_block = "10.0.0.0/24"
  availability_zone = "eu-west-3a"
  map_public_ip_on_launch = true

  tags = {
    Name = "cloudary-pub-subnet-a"
  }

}

resource "aws_subnet" "public_subnet_b" {

  vpc_id = aws_vpc.cloudary_vpc.id
  cidr_block = "10.0.1.0/24"
  availability_zone = "eu-west-3b"
  map_public_ip_on_launch = true

  tags = {
    Name = "cloudary-pub-subnet-b"
  }

}

resource "aws_subnet" "private_subnet_a" {

  vpc_id = aws_vpc.cloudary_vpc.id
  cidr_block = "10.0.16.0/20"
  availability_zone = "eu-west-3a"

  tags = {
    Name = "cloudary-priv-subnet-a"
  }

}

resource "aws_subnet" "private_subnet_b" {

  vpc_id = aws_vpc.cloudary_vpc.id
  cidr_block = "10.0.32.0/20"
  availability_zone = "eu-west-3b"

  tags = {
    Name = "cloudary-priv-subnet-b"
  }

}