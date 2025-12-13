import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./styles/Notifications.module.css";
import Sidebar from "./components/Sidebar";

export default function Notifications() {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchNotifications = async () => {
            const token = localStorage.getItem('token');
            if (!token) {
                navigate('/');
                return;
            }

            try {
                const response = await fetch('http://localhost:8080/api/notifications', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 403) {
                    localStorage.removeItem('token');
                    navigate('/');
                    return;
                }

                if (response.ok) {
                    const data = await response.json();
                    setNotifications(data);
                }
            } catch (err) {
                console.error('Erro:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchNotifications();
    }, [navigate]);

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', hour: '2-digit', minute: '2-digit' });
    };

    return (
        <div className={styles.page}>
            <Sidebar />
            <main className={styles["feed-container"]}>
                <div className={styles["feed-inner"]}>
                    <h2 className={styles.title}>Notificações</h2>

                    {loading ? (
                        <div className={styles.loading}>Carregando notificações...</div>
                    ) : notifications.length === 0 ? (
                        <div className={styles["no-notifications"]}>
                            <h3>Nenhuma notificação</h3>
                            <p>Você ainda não recebeu nenhuma notificação.</p>
                        </div>
                    ) : (
                        notifications.map((notif) => (
                            <div key={notif.id} className={styles["notification-card"]}>
                                <div className={styles.avatar}>
                                    {notif.senderAvatar ? (
                                        <img src={notif.senderAvatar} alt={notif.senderName} />
                                    ) : (
                                        <div className={styles["avatar-placeholder"]}>
                                            {notif.senderName ? notif.senderName.charAt(0).toUpperCase() : '?'}
                                        </div>
                                    )}
                                </div>
                                <div className={styles.content}>
                                    <p className={styles.text}>
                                        <strong>{notif.senderName}</strong> curtiu sua ideia <strong>"{notif.ideaTitle}"</strong>
                                    </p>
                                    <span className={styles.date}>{formatDate(notif.date)}</span>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </main>
        </div>
    );
}