# The template for the ec2 instances
resource "aws_launch_template" "cloudary_launch_template" {

  name_prefix = "cloudary_ec2_"
  image_id = "ami-030ebd4d4694126d2" # Amazon linux 2023 kernel-6.1 TODO: Switch to a custom AMI later with docker/podman installed and ready.
  instance_type = "t3.micro"

  // TODO: Add user data, for a simple deploy, pull the container from dockerhub or ECR, and run it on docker/podman with Watchtower to restart the container when the image updates.

  vpc_security_group_ids = [aws_security_group.ec2_sg.id]
}

# The autoscaling group itself
resource "aws_autoscaling_group" "cloudary_asg" {

  name = "cloudary-asg"
  desired_capacity = 1
  max_size = 1
  min_size = 1

  # Distribute instances across AZs if there was more than one.
  vpc_zone_identifier = [
    aws_subnet.private_subnet_a.id,
    aws_subnet.private_subnet_b.id
  ]

  health_check_type = "ELB"
  health_check_grace_period = 300

  target_group_arns = [aws_lb_target_group.cloudary_target_group.arn]

  launch_template {
    id = aws_launch_template.cloudary_launch_template.id
    version = "$Latest"
  }

}

# The load balancer
resource "aws_lb" "cloudary_alb" {

  name = "cloudary-alb"
  internal = false
  load_balancer_type = "application"
  security_groups = [aws_security_group.alb_sg.id]

  subnets = [
    aws_subnet.public_subnet_a.id,
    aws_subnet.public_subnet_b.id
  ]

}

# The target group attached to the ASG, which is pointed to by the ALB.
resource "aws_lb_target_group" "cloudary_target_group" {
  name = "cloudary-tg"
  port = 80
  protocol = "HTTP"
  vpc_id = aws_vpc.cloudary_vpc.id

  health_check {
    path = "/actuator/health"
    healthy_threshold = 2
    unhealthy_threshold = 2
    timeout = 5
    interval = 30
    matcher = "200"
  }
}

# The HTTP listener for the ALB
resource "aws_lb_listener" "cloudary_alb_listener_http" {

  load_balancer_arn = aws_lb.cloudary_alb.arn
  port = 80
  protocol = "HTTP"

  default_action {
    type = "forward"
    target_group_arn = aws_lb_target_group.cloudary_target_group.arn
  }
}

# The HTTPS listener, need to set up a cert in ACM first.
/*
resource "aws_lb_listener" "cloudary_alb_listener_https" {

  load_balancer_arn = aws_lb.cloudary_alb.arn
  port = 443
  protocol = "HTTPS"

  default_action {
    type = "forward"
    target_group_arn = aws_lb_target_group.cloudary_target_group.arn
  }
}*/

# ########################
# SECURITY GROUPS
# ########################

resource "aws_security_group" "ec2_sg" {

  name = "ec2-sg"
  vpc_id = aws_vpc.cloudary_vpc.id
  description = "Permits port 80 HTTP traffic from the ALB only."

  ingress {
    from_port = 80
    to_port = 80
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

resource "aws_security_group" "alb_sg" {

  name = "alb-sg"
  vpc_id = aws_vpc.cloudary_vpc.id
  description = "Permits port 80 and port 443 traffic from the internet."

  # Useless for now, uncomment when ACM is set up for HTTPS at the load balancer.
  /*
  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }*/

  ingress {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}