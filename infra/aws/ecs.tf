# Log group for the application logs.
resource "aws_cloudwatch_log_group" "cloudary" {
  name              = "/ecs/cloudary"
  retention_in_days = 14
}

resource "aws_ecs_cluster" "cloudary_cluster" {
  name = "cloudary-cluster"
}

resource "aws_ecs_task_definition" "app_task_def" {

  # Should depend on these since if the DB and cache are not created first, the ECS task will fail on startup.
  depends_on = [aws_db_instance.cloudary_rds, aws_elasticache_cluster.cloudary_cache]

  family = "cloudary"
  requires_compatibilities = ["FARGATE"]
  network_mode = "awsvpc"
  cpu = "512"
  memory = "3072"
  execution_role_arn = aws_iam_role.ecs_execution_role.arn
  task_role_arn = aws_iam_role.ecs_execution_role.arn

  container_definitions = jsonencode([
    {
      name = "cloudary-app"
      image = "public.ecr.aws/a2f9s1p8/cdcrane/cloudary:latest"
      portMappings = [{
        containerPort = 8080
      }]
      environment = [
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.cloudary_rds.address}:5432/${aws_db_instance.cloudary_rds.db_name}" },
        { name = "SPRING_DATASOURCE_USERNAME", value = aws_db_instance.cloudary_rds.username },
        { name = "SPRING_DATA_REDIS_HOST", value = aws_elasticache_cluster.cloudary_cache.cache_nodes[0].address },
        { name = "AWS_S3_UPLOADS_BUCKET_NAME", value = aws_s3_bucket.cloudary_uploads_bucket.bucket },
        { name  = "JAVA_TOOL_OPTIONS", value = "-Xms512m -Xmx1024m" }
        # TODO: Add email creds using SSM, provided via variables.
      ]
      secrets = [
        {
          name = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = aws_ssm_parameter.rds_master_pass.arn
        }
      ]
      essential = true
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group = aws_cloudwatch_log_group.cloudary.name
          awslogs-region = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])

}

resource "aws_ecs_service" "cloudary_service" {
  name = "cloudary-ecs-service"
  cluster = aws_ecs_cluster.cloudary_cluster.id
  task_definition = aws_ecs_task_definition.app_task_def.arn
  desired_count = 1
  launch_type = "FARGATE"

  network_configuration {
    subnets = [aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id]
    security_groups = [aws_security_group.ecs_sg.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.cloudary_target_group.arn
    container_name = "cloudary-app"
    container_port = 8080
  }
}



resource "aws_security_group" "ecs_sg" {
  name = "cloudary-sg"
  vpc_id = aws_vpc.cloudary_vpc.id

  ingress {
    from_port = 8080
    to_port = 8080
    protocol = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Allows ecs to assume a role.
resource "aws_iam_role" "ecs_execution_role" {

  name = "ecsTaskExecutionRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = { Service : "ecs-tasks.amazonaws.com"}
      Action = "sts:AssumeRole"
    }]
  })

}

# Assigning ecs the execution role.
resource "aws_iam_role_policy_attachment" "ecs_execution_role_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}