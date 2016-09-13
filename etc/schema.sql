CREATE TABLE seen_blog_url (
  id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
  blog_url VARCHAR(1024),
  feed_url VARCHAR(1024),
  seen DATETIME
) ENGINE=InnoDB CHARACTER SET=utf8mb4;

-- Typical URLs are ~50 characters.
CREATE INDEX blog_url_name ON seen_blog_url(blog_url(128));
