import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './Sidebar.module.css';

import home from '../assets/home.png';
import home_hover from '../assets/home_hover.png';
import coracao from '../assets/coracao.png';
import coracao_hover from '../assets/coracao_hover.png';
import salvo from '../assets/salvo.png';
import salvo_hover from '../assets/salvo_hover.png';
import lupa from '../assets/lupa.png';
import lupa_hover from '../assets/lupa_hover.png';
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
    const [profileImage, setProfileImage] = useState(null);
    const navigate = useNavigate();

    const token = localStorage.getItem('token');
    const userData = token ? parseJwt(token) : null;
    const isInvestidor = userData?.role === 'INVESTIDOR';

    React.useEffect(() => {
        const loadProfileImage = async () => {
            // 1. Tenta pegar do localStorage primeiro (mais rápido)
            const savedData = localStorage.getItem('userProfileData');
            if (savedData) {
                try {
                    const parsed = JSON.parse(savedData);
                    if (parsed.profileImageUrl || parsed.pfp) {
                        setProfileImage(parsed.profileImageUrl || parsed.pfp);
                        return;
                    }
                } catch (e) {
                    console.error("Erro ao ler localStorage", e);
                }
            }

            // 2. Se não tiver no localStorage ou não tiver imagem, busca do backend
            if (userData?.id) {
                try {
                    const response = await fetch(`http://localhost:8080/user/${userData.id}`, {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    });
                    if (response.ok) {
                        const data = await response.json();
                        if (data.pfp) {
                            setProfileImage(data.pfp);
                            // Atualiza localStorage para próxima vez
                            if (savedData) {
                                const parsed = JSON.parse(savedData);
                                parsed.profileImageUrl = data.pfp;
                                localStorage.setItem('userProfileData', JSON.stringify(parsed));
                            }
                        }
                    }
                } catch (error) {
                    console.error("Erro ao buscar imagem de perfil", error);
                }
            }
        };

        loadProfileImage();
    }, [userData?.id, token]);

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('userProfileData');
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

                <button onClick={() => navigate('/perfil/search')} className={styles["icon-btn"]}>
                    <img src={hovered ? lupa_hover : lupa} alt="Usuários" />
                    <span>Usuários</span>
                </button>

                {!isInvestidor && (
                    <button onClick={() => navigate('/postar-ideia')} className={styles["icon-btn"]}>
                        <img src={hovered ? mais_hover : mais} alt="Postar" />
                        <span>Postar</span>
                    </button>
                )}
            </nav>

            <div className={styles.profile}>
                <button className={styles["profile-btn"]} onClick={() => userData && navigate(`/perfil/${userData.id}`)}>
                    <img
                        src={profileImage || usuario}
                        alt="Perfil"
                        style={{ borderRadius: '50%', objectFit: 'cover' }}
                    />
                    <span>Perfil</span>
                </button>
                <button className={styles["profile-out"]} onClick={handleLogout}>
                    <img src={logout} alt="Sair" />
                </button>
            </div>
        </aside>
    );
}