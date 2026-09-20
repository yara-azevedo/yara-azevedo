from flask import Flask #importanto a classe flask da biblioteca flask
from flask_sqlalchemy import SQLAlchemy


app = Flask(__name__) #instancia o flask
app.config['SQLAlchemy_DATABASE_URI'] = 'sqlite:///ecommerce.db'

db = SQLAlchemy(app)


class Product(db.Model):
        id = db.Column(db.Integer, primary_key=True)
        name = db.Column(db.String(120), nullable=False)
        price = db.Column(db.Float, nullable=False)
        description = db.Column(db.Text, nullable=True) #text nao tem limitacao de tamanho, diferente do String 



#rotas de comunicacao com a api, as portas, endereço - endpoint

#definir uma rota raiz da pagina inicial e a funcao que vai ser executada numa requisicao

@app.route('/')
def hellow():
    return 'hellow';

if __name__ == '__main__':
    app.run(debug=True) #metodo true é só pra desenvolvimento, n pode ir pra live

