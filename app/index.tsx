import { View, Text, StyleSheet, ScrollView } from 'react-native';

export default function Index() {
  return (
    <ScrollView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>AutoAccept Project</Text>
        <Text style={styles.description}>
          This project contains a native Android app for auto-accepting orders.
        </Text>
        <Text style={styles.subtitle}>Android App Location:</Text>
        <Text style={styles.code}>android-autoaccept/</Text>
        <Text style={styles.instructions}>
          To use the Android app:
        </Text>
        <Text style={styles.step}>1. Open the android-autoaccept folder in Android Studio</Text>
        <Text style={styles.step}>2. Build and run on an Android device (API 26+)</Text>
        <Text style={styles.step}>3. Enable Accessibility Service when prompted</Text>
        <Text style={styles.step}>4. Configure settings and toggle "Enable Auto-Accept"</Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  content: {
    padding: 24,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 16,
  },
  description: {
    fontSize: 16,
    marginBottom: 24,
    lineHeight: 24,
  },
  subtitle: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 8,
  },
  code: {
    fontSize: 16,
    fontFamily: 'monospace',
    backgroundColor: '#f5f5f5',
    padding: 12,
    borderRadius: 4,
    marginBottom: 24,
  },
  instructions: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 12,
  },
  step: {
    fontSize: 16,
    marginBottom: 8,
    lineHeight: 24,
  },
});
