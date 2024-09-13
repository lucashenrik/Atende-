import React from "react";

export const BoaTarde = props => <h1>Boa Tarde {props.nome}! </h1>

const BoaNoite = props => <h1> Boa Noite {props.nome}! </h1>

export { BoaNoite }

export default { BoaTarde, BoaNoite }