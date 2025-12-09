import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './Sidebar.module.css';

import home from '../assets/home.png';
import home_hover from '../assets/home_hover.png';
import coracao from '../assets/coracao.png';
import coracao_hover from '../assets/coracao_hover.png';
import seta from '../assets/seta.png';
import seta_hover from '../assets/seta_hover.png';
import mais from '../assets/mais.png';
import mais_hover from '../assets/mais_hover.png';
import usuario from '../assets/usuario.png';

export default function Sidebar() {
    const [hovered, setHovered] = useState(false);
    const navigate = useNavigate();

    return (
        <aside
            className={styles.sidebar}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
        >
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
                    <img src={hovered ? seta_hover : seta} alt="Salvos" />
                    <span>Salvos</span>
                </button>

                <button onClick={() => navigate('/postar-ideia')} className={styles["icon-btn"]}>
                    <img src={hovered ? mais_hover : mais} alt="Postar" />
                    <span>Postar</span>
                </button>
            </nav>

            <div className={styles.profile}>
                <button className={styles["profile-btn"]}>
                    <img src={usuario} alt="Perfil" />
                    <span>Perfil</span>
                </button>
            </div>
        </aside>
    );
}