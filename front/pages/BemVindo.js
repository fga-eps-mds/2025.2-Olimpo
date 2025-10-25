import { useState } from "react";
import {Text, StyleSheet, View, Image, Pressable} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Color, FontFamily, Height, FontSize, Border } from "../style/GlobalStyles";



const BemVindo = () => {
    const [CriarPress, setCriarPress] = useState(false);
    const [LoginPress, setLoginPress] = useState(false);
    
    return (
        <SafeAreaView style={styles.container}>
            <View style={styles.content}>
                <Image 
                    source={require('./imagens/logo.png')} 
                    style={styles.logo}
                    resizeMode="contain"
                />
                <Text style={styles.lume}>Lume</Text>
                <Text style={styles.conectandoIdeias}>
                    Conectando ideias inovadoras a investidores que acreditam no potencial universitário
                </Text>
                
                <Pressable 
                    style={styles.containerBot}
                    onPressIn={() => setCriarPress(true)}
                    onPressOut={() => setCriarPress(false)}
                >
                    <View style={[
                        styles.botao,
                        CriarPress && styles.botaoPress
                    ]} />
                    <Text style={[
                        styles.textoBot,
                        CriarPress && styles.textoBotPress
                    ]}>
                        Criar conta
                    </Text>
                </Pressable>
                
                <Pressable 
                    style={styles.containerBot}
                    onPressIn={() => setLoginPress(true)}
                    onPressOut={() => setLoginPress(false)}
                >
                    <View style={[
                        styles.botao,
                        LoginPress && styles.botaoPress
                    ]} />
                    <Text style={[
                        styles.textoBot,
                        LoginPress && styles.textoBotPress
                    ]}>
                        Já tenho uma conta
                    </Text>
                </Pressable>


                <Text style={styles.transformeSuaIdeia}>
                    Transforme sua ideia em realidade ou invista no futuro
                </Text>
            </View>
        </SafeAreaView>
    );
};



const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: Color.colorGray,
    },
    content: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        paddingHorizontal: 30,
    },
    logo: {
        width: 118,
        height: 118,
        marginBottom: 18
    },
    lume: {
        fontSize: 36,
        color: Color.colorWhite,
        textAlign: "center",
        fontFamily: FontFamily.arial,
        marginBottom: 16
    },
    conectandoIdeias: {
        fontSize: FontSize.fs_15,
        color: Color.colorLightsteelblue,
        textAlign: "center",
        fontFamily: FontFamily.arial,
        width: 313,
        marginBottom: 24
    },
    containerBot: {
        width: 330,
        height: Height.height_40,
        marginBottom: 12,
        position: 'relative'
    },
    botao: {
        width: '100%',
        height: '100%',
        borderRadius: Border.br_10,
        backgroundColor: Color.colorWhitesmoke,
        position: 'absolute'
    },
    botaoPress: {
        backgroundColor: "#987700",
        opacity: 0.8
    },
    textoBot: {
        color: Color.colorGray,
        fontSize: FontSize.fs_15,
        textAlign: "center",
        fontFamily: FontFamily.arial,
        position: 'absolute',
        width: '100%',
        top: '27.5%'
    },
    textoBotPress: {
        color: Color.colorWhite
    },
    transformeSuaIdeia: {
        fontSize: 14,
        color: Color.colorLightsteelblue,
        textAlign: "center",
        fontFamily: FontFamily.arial,
        width: 313,
        marginTop: 8
    }
});

export default BemVindo;