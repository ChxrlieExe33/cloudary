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

# The target group attached to the cluster, which is pointed to by the ALB.
resource "aws_lb_target_group" "cloudary_target_group" {
  name = "cloudary-tg"
  port = 8080
  protocol = "HTTP"
  vpc_id = aws_vpc.cloudary_vpc.id
  target_type = "ip"

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

# The security group for the load balancer, allowing inbound port 80 HTTP traffic.
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