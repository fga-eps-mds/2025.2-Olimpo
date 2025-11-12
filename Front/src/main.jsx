import React from 'react'
import ReactDOM from 'react-dom/client'
import {
  createBrowserRouter,
  RouterProvider,
} from "react-router-dom";

import './styles/index.css' 

import App from './App.jsx'
import EsqueciSenha from './EsqueciSenha.jsx'
import LinkEnviado from './LinkEnviado.jsx'
import CadastroEstudante from './CadastroEstudante.jsx'
import CadastroInvestidor from './CadastroInvestidor.jsx'
// import VerificaEmail from './VerificaEmail.jsx' // <-- DELETAR/COMENTAR
import StatusVerificacao from './StatusVerificacao.jsx' // <-- IMPORTAR
import ResetarSenha from './ResetarSenha.jsx'

const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
  },
  {
    path: "/esqueci-senha",
    element: <EsqueciSenha />,
  },
  {
    path: "/link-enviado",
    element: <LinkEnviado />,
  },
  {
    path: "/cadastro-estudante",
    element: <CadastroEstudante />,
  },
  {
    path: "/cadastro-investidor",
    element: <CadastroInvestidor />,
  },
  {
    path: "/verificacao/:status",
    element: <StatusVerificacao />,
  },
  {
    path: "/reset-password",
    element: <ResetarSenha />,
  }
]);

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <RouterProvider router={router} />
  </React.StrictMode>,
)