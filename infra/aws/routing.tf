resource "aws_eip" "nat_gateway_eip" {

  tags = {
    Name = "cloudary-nat-gateway-eip"
  }
}

resource "aws_route_table" "pub_rt" {
  vpc_id = aws_vpc.cloudary_vpc.id

  # Only provide the public internet one, no need to add the local one.
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.cloudary_igw.id
  }

  tags = {
    Name = "cloudary-pub-rt"
  }
}

resource "aws_route_table" "priv_rt" {
  vpc_id = aws_vpc.cloudary_vpc.id

  # Only provide the public internet one, no need to add the local one.
  route {
    cidr_block = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat_gw.id
  }

  tags = {
    Name = "cloudary-priv-rt"
  }
}


resource "aws_route_table_association" "pub_rt_association_a" {
  route_table_id = aws_route_table.pub_rt.id
  subnet_id = aws_subnet.public_subnet_a.id
}

resource "aws_route_table_association" "pub_rt_association_b" {
  route_table_id = aws_route_table.pub_rt.id
  subnet_id = aws_subnet.public_subnet_b.id
}


resource "aws_route_table_association" "priv_rt_association_a" {
  route_table_id = aws_route_table.priv_rt.id
  subnet_id = aws_subnet.private_subnet_a.id
}

resource "aws_route_table_association" "priv_rt_association_b" {
  route_table_id = aws_route_table.priv_rt.id
  subnet_id = aws_subnet.private_subnet_b.id
}



resource "aws_nat_gateway" "nat_gw" {

  subnet_id = aws_subnet.public_subnet_a.id
  allocation_id = aws_eip.nat_gateway_eip.id

  tags = {
    Name = "cloudary-nat-gw"
  }

}