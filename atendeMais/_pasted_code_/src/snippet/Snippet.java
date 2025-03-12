package snippet;

public class Snippet {
	# Ativa o SSL e informa o tipo do keystore
	server.ssl.enabled=true
	server.ssl.key-store-type=PKCS12
	
	# Caminho absoluto para o keystore gerado
	server.ssl.key-store=/etc/letsencrypt/live/atende-mais.shop/keystore.p12
	
	# Senha definida no momento da exportação do keystore
	server.ssl.key-store-password=@01020304Ll
	
	# Alias definido na exportação
	server.ssl.key-alias=atende-mais-shop
}

