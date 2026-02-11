terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "cloudary-state-bucket"
    key            = "env/prod/terraform.tfstate"
    region         = "eu-west-3"
    dynamodb_table = "terraform-locks-cloudary"
    encrypt        = true
  }
}

provider "aws" {
  region = "eu-west-3"
}