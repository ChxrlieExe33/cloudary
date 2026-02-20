resource "random_password" "db_master_pass" {
  length           = 20
  special          = true
  override_special = "!#$%^&*()-_=+[]{}<>?"
}

# Postgres DB instance.
resource "aws_db_instance" "cloudary_rds" {

  identifier = "cloudary-rds"
  instance_class = "db.t3.micro"
  engine = "postgres"
  allocated_storage = 5
  storage_type = "gp2"
  username = "cloudary_masteruser"
  password = random_password.db_master_pass.result
  db_name = "cloudary"
  skip_final_snapshot = true
  publicly_accessible = false
  multi_az = false

  vpc_security_group_ids = [aws_security_group.cloudary_db_sg.id]

  db_subnet_group_name = aws_db_subnet_group.rds_subnet_group.name

  iam_database_authentication_enabled = true

  tags = {
    Name = "cloudary-rds"
  }

}

# Redis cache.
resource "aws_elasticache_cluster" "cloudary_cache" {
  cluster_id = "cloudary-cache"
  engine = "redis"
  node_type = "cache.t3.micro"
  num_cache_nodes = 1
  parameter_group_name = "default.redis7"
  port = 6379
  subnet_group_name = aws_elasticache_subnet_group.cloudary_cache_subnet_group.name
  security_group_ids = [aws_security_group.cloudary_cache_sg.id]

  tags = {
    Name = "CloudaryCache"
  }

}

# ###################################################
# SUBNET GROUPS
# ###################################################

resource "aws_db_subnet_group" "rds_subnet_group" {
  name = "cloudary-db-subnet-group"
  subnet_ids = [aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id]
  tags = {
    Name = "cloudary-db-subnet-group"
  }
}

resource "aws_elasticache_subnet_group" "cloudary_cache_subnet_group" {
  name = "cloudary-cache-subnet-group"
  subnet_ids = [aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id]
  tags = {
    Name = "cloudary-cache-subnet-group"
  }
}

# ###################################################
# SECURITY GROUPS
# ###################################################

resource "aws_security_group" "cloudary_db_sg" {
  name = "cloudary-db-sg"
  vpc_id = aws_vpc.cloudary_vpc.id

  ingress {
    to_port = 5432
    from_port = 5432
    protocol = "tcp"
    cidr_blocks = [aws_subnet.private_subnet_b.cidr_block, aws_subnet.private_subnet_a.cidr_block] # Only allow inbound from the private subnets.
  }

  egress { # Allow all egress
    to_port = 0
    from_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "cloudary-db-sg"
  }
}

resource "aws_security_group" "cloudary_cache_sg" {
  name = "cache-subnet-group"
  vpc_id = aws_vpc.cloudary_vpc.id

  ingress {
    from_port = 6379
    to_port = 6379
    protocol = "tcp"
    cidr_blocks = [aws_subnet.private_subnet_b.cidr_block, aws_subnet.private_subnet_a.cidr_block] # Only allow inbound from the private subnets.
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}