PRAGMA key = "x'INSERTHERE'";
ATTACH DATABASE 'plaintext.db' AS plaintext KEY '';
SELECT sqlcipher_export('plaintext');
DETACH DATABASE plaintext;
