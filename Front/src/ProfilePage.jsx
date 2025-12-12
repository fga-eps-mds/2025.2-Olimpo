import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import VisualizarPerfil from './VisualizarPerfil';
import VisualizarPerfilOutros from './VisualizarPerfilOutros';

function ProfilePage() {
    const { id } = useParams();
    const [isOwner, setIsOwner] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const checkOwner = () => {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const payload = JSON.parse(atob(token.split('.')[1]));
                    const currentUserId = payload.id;
                    // Comparação frouxa (==) para lidar com string vs number
                    setIsOwner(currentUserId == id);
                } catch (e) {
                    console.error("Erro ao decodificar token", e);
                    setIsOwner(false);
                }
            } else {
                setIsOwner(false);
            }
            setLoading(false);
        };

        checkOwner();
    }, [id]);

    if (loading) {
        return <div>Carregando...</div>;
    }

    return isOwner ? <VisualizarPerfil /> : <VisualizarPerfilOutros />;
}

export default ProfilePage;
