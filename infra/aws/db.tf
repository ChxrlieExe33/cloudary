resource "random_password" "db_master_pass" {
  length = 16
  special = true
}

resource "aws_db_subnet_group" "rds_subnet_group" {

  name = "cloudary-db-subnet-group"

  subnet_ids = [
    aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id
  ]

  tags = {
    Name = "cloudary-db-subnet-group"
  }

}

resource "aws_security_group" "cloudary_db_sg" {
  name = "cloudary-db-sg"
  vpc_id = aws_vpc.cloudary_vpc.id

  ingress {
    to_port = 5432
    from_port = 5432
    protocol = "tcp"
    cidr_blocks = [aws_vpc.cloudary_vpc.cidr_block]
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

# Still need to create the cloudary_app postgres user (for IAM auth), assign the rds_iam grant role, and grant ALL on cloudary db.
# Below is a potential solution with problems.

# ########################################################################
# POSTGRES PROVIDER TO CREATE DB USER AND ASSIGN PERMISSIONS FOR IAM AUTH
#
# Working on a solution for this part, since the db is in a
# private subnet, tf won't be able to connect from local to
# make these changes.
# #########################################################################
/*
provider "postgresql" {
  host = aws_db_instance.cloudary_rds.address
  port = 5432
  username = aws_db_instance.cloudary_rds.username
  password = random_password.db_master_pass.result
  sslmode = "require"
}

resource "postgresql_role" "application_db_user" {
  name = "cloudary_app"
  login = true
}

resource "postgresql_grant_role" "application_db_user_rds_iam" {
  grant_role = "rds_iam"
  role       = postgresql_role.application_db_user.name
}

resource "postgresql_grant" "application_db_user_perms" {
  database    = aws_db_instance.cloudary_rds.db_name
  privileges = ["ALL"]
  role        = postgresql_role.application_db_user.name
  object_type = "database"
}*/