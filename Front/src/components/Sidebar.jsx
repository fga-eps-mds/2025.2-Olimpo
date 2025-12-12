import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './Sidebar.module.css';

import home from '../assets/home.png';
import home_hover from '../assets/home_hover.png';
import coracao from '../assets/coracao.png';
import coracao_hover from '../assets/coracao_hover.png';
import salvo from '../assets/salvo.png';
import salvo_hover from '../assets/salvo_hover.png';
import mais from '../assets/mais.png';
import mais_hover from '../assets/mais_hover.png';
import usuario from '../assets/usuario.png';
import logo from '../assets/logo.png'
import icon from '../assets/icon.png'
import logout from '../assets/logout.png'

const parseJwt = (token) => {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch {
        return null;
    }
};

export default function Sidebar() {
    const [hovered, setHovered] = useState(false);
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/');
    };

    return (
        <aside
            className={styles.sidebar}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
        >
            <div className={styles.logo}>
                <img src={hovered ? logo : icon} alt="Logo" />
            </div>

            <nav className={styles["menu-icons"]}>
                <button onClick={() => navigate('/home')} className={styles["icon-btn"]}>
                    <img src={hovered ? home_hover : home} alt="Início" />
                    <span>Início</span>
                </button>

                <button onClick={() => navigate('/notificacoes')} className={styles["icon-btn"]}>
                    <img src={hovered ? coracao_hover : coracao} alt="Notificações" />
                    <span>Notificações</span>
                </button>

                <button onClick={() => navigate('/salvos')} className={styles["icon-btn"]}>
                    <img src={hovered ? salvo_hover : salvo} alt="Salvos" />
                    <span>Salvos</span>
                </button>

                {!isInvestidor && (
                    <button onClick={() => navigate('/postar-ideia')} className={styles["icon-btn"]}>
                        <img src={hovered ? mais_hover : mais} alt="Postar" />
                        <span>Postar</span>
                    </button>
                )}
            </nav>

            <div className={styles.profile}>
                <button className={styles["profile-btn"]}>
                    <img src={usuario} alt="Perfil" />
                    <span>Perfil</span>
                </button>
                <button className={styles["profile-out"]} onClick={handleLogout}>
                    <img src={logout} alt="Sair" />
                </button>
            </div>
        </aside>
    );
}