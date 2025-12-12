import React, { useState, useEffect, useRef } from "react";


import { useNavigate } from "react-router-dom";


import Sidebar from "./components/Sidebar";


import styles from "./styles/EditarPerfil.module.css";





export default function EditarPerfil() {


    const navigate = useNavigate();





    const [username, setUsername] = useState("");


    const [curso, setCurso] = useState("");


    const [faculdade, setFaculdade] = useState("");


    const [descricao, setDescricao] = useState("");


    const [fotoPerfil, setFotoPerfil] = useState(null);


    const [fotoAtual, setFotoAtual] = useState("");


    const [userEmail, setUserEmail] = useState("");


    const [loading, setLoading] = useState(false);


    const [errorMessage, setErrorMessage] = useState("");





    const fileInputRef = useRef(null);





    useEffect(() => {


        const token = localStorage.getItem('token');


        if (!token) {


            navigate('/login');


            return;


        }





        const savedData = localStorage.getItem('userProfileData');


        if (savedData) {


            try {


                const userData = JSON.parse(savedData);


                console.log("Dados carregados do localStorage:", userData);





                setUserEmail(userData.email || "");


                setUsername(userData.username || userData.name || userData.email?.split('@')[0] || "");


                setCurso(userData.course || userData.curso || "");


                setFaculdade(userData.university || userData.faculdade || userData.college || "");


                setDescricao(userData.description || userData.descricao || userData.bio || "");


                setFotoAtual(userData.profileImageUrl || userData.imageUrl || userData.avatar || "");


            } catch (error) {


                console.error("Erro ao carregar dados:", error);


            }


        }





        if (!savedData || !userEmail) {


            try {


                const tokenParts = token.split('.');


                if (tokenParts.length === 3) {


                    const payload = JSON.parse(atob(tokenParts[1]));


                    const email = payload.sub;


                    setUserEmail(email);


                    if (!username) {


                        setUsername(email.split('@')[0]);


                    }


                }


            } catch (error) {


                console.error("Erro ao decodificar token:", error);


            }


        }


    }, [navigate]);





    const handleAlterarFotoClick = () => {


        fileInputRef.current?.click();


    };





    const handleFotoChange = (e) => {


        if (e.target.files?.[0]) {


            const file = e.target.files[0];


            setFotoPerfil(file);





            const reader = new FileReader();


            reader.onload = (event) => {


                setFotoAtual(event.target.result);


            };


            reader.readAsDataURL(file);


        }


    };





    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage("");
        setLoading(true);

        const token = localStorage.getItem('token');
        if (!token) {
            alert("Usuário não autenticado");
            navigate('/login');
            return;
        }

        try {
            const profileData = {
                name: username,
                email: userEmail,
                curso: curso,
                faculdade: faculdade,
                bio: descricao
            };

            const formData = new FormData();
            formData.append('data', JSON.stringify(profileData));

            if (fotoPerfil && typeof fotoPerfil === 'object') {
                formData.append('photo', fotoPerfil);
            }

            const response = await fetch('http://localhost:8080/user/profile', {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (response.ok) {
                const updatedProfile = await response.json();

                // Update local storage with new data
                const localStorageData = {
                    email: updatedProfile.email,
                    username: updatedProfile.name,
                    name: updatedProfile.name,
                    curso: updatedProfile.curso,
                    faculdade: updatedProfile.faculdade,
                    bio: updatedProfile.bio,
                    profileImageUrl: updatedProfile.pfp,
                    updatedAt: new Date().toISOString()
                };
                localStorage.setItem('userProfileData', JSON.stringify(localStorageData));

                alert("Perfil atualizado com sucesso!");
                navigate(`/perfil/${updatedProfile.id}`);
            } else {
                const errorText = await response.text();
                setErrorMessage(errorText || "Erro ao atualizar perfil.");
            }
        } catch (error) {
            console.error("Erro ao atualizar perfil:", error);
            setErrorMessage("Erro ao conectar com o servidor. Tente novamente.");
        } finally {
            setLoading(false);
        }
    };





    const handleCancel = () => {


        navigate('/home');


    };





    return (


        <div className={styles["container-root"]}>


            <Sidebar />


            <main className={styles["main-content"]}>


                <div className={styles["form-container"]}>


                    <form className={styles["post-form"]} onSubmit={handleSubmit}>





                        <div className={styles["foto-container"]}>


                            <div className={`${styles["foto-perfil"]} ${!fotoAtual ? styles["foto-perfil-sem-imagem"] : ""}`}>


                                {fotoAtual ? (


                                    <img


                                        src={fotoAtual}


                                        alt="Foto de perfil"


                                        className={styles["foto-perfil-img"]}


                                        onError={(e) => {


                                            e.target.style.display = 'none';


                                            e.target.parentElement.classList.add(styles["foto-perfil-sem-imagem"]);


                                        }}


                                    />


                                ) : (


                                    <svg


                                        className={styles["foto-perfil-img"]}


                                        viewBox="0 0 24 24"


                                        fill="none"


                                        stroke="#1e1e2e"


                                        strokeWidth="2"


                                    >


                                        <circle cx="12" cy="7" r="4" />


                                        <path d="M5.5 21a8.38 8.38 0 0 1 13 0" />


                                    </svg>


                                )}


                            </div>





                            <input


                                type="file"


                                accept="image/*"


                                ref={fileInputRef}


                                style={{ display: 'none' }}


                                onChange={handleFotoChange}


                            />





                            <button


                                type="button"


                                className={styles["btn-alterar-foto"]}


                                onClick={handleAlterarFotoClick}


                            >


                                Alterar foto


                            </button>





                            {fotoPerfil && (


                                <span className={styles["mensagem-imagem"]}>


                                    Nova imagem selecionada


                                </span>


                            )}


                        </div>





                        <div style={{ width: "100%" }}>


                            <label className={styles.label}>Nome de usuário</label>


                            <input


                                className={styles.input}


                                type="text"


                                placeholder="User.perfil"


                                value={username}


                                onChange={(e) => setUsername(e.target.value)}


                                required


                                disabled={loading}


                            />


                            {userEmail && (


                                <small style={{ color: '#666', display: 'block', marginTop: '5px' }}>


                                    Email: {userEmail}


                                </small>


                            )}


                        </div>





                        <div className={styles["input-row"]}>


                            <div style={{ width: "100%" }}>


                                <label className={styles.label}>Curso</label>


                                <input


                                    className={styles.input}


                                    type="text"


                                    placeholder="Ex: Ciência da Computação"


                                    value={curso}


                                    onChange={(e) => setCurso(e.target.value)}


                                    disabled={loading}


                                />


                            </div>





                            <div style={{ width: "100%" }}>


                                <label className={styles.label}>Faculdade</label>


                                <input


                                    className={styles.input}


                                    type="text"


                                    placeholder="Ex: Universidade Federal"


                                    value={faculdade}


                                    onChange={(e) => setFaculdade(e.target.value)}


                                    disabled={loading}


                                />


                            </div>


                        </div>





                        <div style={{ width: "100%" }}>


                            <label className={styles.label}>Descrição</label>


                            <textarea


                                className={styles.textarea}


                                placeholder="Adicione uma descrição sobre você..."


                                value={descricao}


                                onChange={(e) => setDescricao(e.target.value)}


                                rows="5"


                                disabled={loading}


                            />


                        </div>





                        {errorMessage && (


                            <div style={{


                                padding: '10px',


                                backgroundColor: '#fff5f5',


                                border: '1px solid #feb2b2',


                                borderRadius: '5px',


                                margin: '15px 0',


                                color: '#c53030'


                            }}>


                                {errorMessage}


                            </div>


                        )}





                        <div className={styles["botoes-container"]}>


                            <button


                                type="button"


                                className={styles["btn-cancelar"]}


                                onClick={handleCancel}


                                disabled={loading}


                            >


                                Cancelar


                            </button>





                            <button


                                type="submit"


                                className={styles["btn-salvar"]}


                                disabled={loading}


                            >


                                {loading ? 'Salvando...' : 'Salvar alterações'}


                            </button>


                        </div>





                    </form>


                </div>


            </main>


        </div>


    );


}