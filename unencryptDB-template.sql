PRAGMA key = "x'INSERTKEY'";
ATTACH DATABASE 'INSERTFILENAME' AS plaintext KEY '';
SELECT sqlcipher_export('plaintext');
DETACH DATABASE plaintext;
