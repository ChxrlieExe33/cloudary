# Cloudary

Cloudary is a file storage platform, which I am building to improve my cloud skills.

The idea is basically a text-based file storage platform, where you can upload files, like txt, code files, markdown, PDF, etc.

Once uploaded, a background job or maybe even a lambda (I haven't decided yet) will extract all text from the file and save it into elasticsearch.

With that, the user can do a full-text search for a file containing a specific string.

Files will be stored in S3.

# Details

- Development environment will use docker compose postgres, but production will use RDS postgres, authenticated via IAM roles.
- Will set up profile-dependent data sources for the above.
- Caching with redis
- JWT authentication with refresh tokens.
- etc

# How to run locally

NOTE: I only just started this, so the stuff for S3 isn't in here yet.

Run the docker compose for the dev setup.

Set the following environment variables before running:

- SPRING_MAIL_USERNAME=youremail@something.com
- SPRING_MAIL_PASSWORD=yourpassword
- SPRING_MAIL_HOST=smtp.gmail.com (or whatever it is)
- SPRING_MAIL_PORT=587

If you're using the local docker compose for postgres and redis, you don't need to set DB properties, otherwise, change them as necessary.

Also, it's recommended to change all the properties under `jwt` like the access token secret and refresh token secret.

