import React from 'react';
import {
  Alert,
  Button,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  type TextStyle,
  View,
} from 'react-native';
import {
  authorizeMobilePaymentsSdk,
  deauthorizeMobilePaymentsSdk,
  forgetReader,
  getAuthorizationLocation,
  getAuthorizationState,
  getReaders,
  presentSettings,
  ReaderEventType,
  type ReaderInfo,
  ReaderState,
  type SquareError,
  startPairing,
  stopPairing,
  useSquareEventListener,
} from 'rn-square-mobile-payments';
import { PERMISSIONS, request } from 'react-native-permissions';

export default function App() {
  const [pairedReaders, setPairedReaders] = React.useState<ReaderInfo[]>([]);
  React.useEffect(() => {
    request(
      Platform.select({
        android: PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION,
        default: PERMISSIONS.IOS.LOCATION_WHEN_IN_USE,
      })
    ).then((result) => {
      if (result !== 'granted') {
        Alert.alert(
          'Permissions not granted',
          'This demo needs your location in order to work properly.'
        );
      }
    });
    request(
      Platform.select({
        android: PERMISSIONS.ANDROID.BLUETOOTH_CONNECT,
        default: PERMISSIONS.IOS.BLUETOOTH,
      })
    ).then((result) => {
      if (result !== 'granted') {
        Alert.alert(
          'Permissions not granted',
          'This demo needs your location in order to work properly.'
        );
      }
    });

    const loadReaders = async () => {
      try {
        let readers = await getReaders();
        setPairedReaders(readers);
      } catch (error) {
        console.log('Error getting: ', JSON.stringify(error, null, 2));
      }
    };

    loadReaders();
  }, []);

  useSquareEventListener(ReaderEventType.READER_ADDED, ({ readerInfo }) => {
    console.log('Reader Added Event with serial: ', readerInfo.serialNumber);
    setPairedReaders((prev) => [
      readerInfo,
      ...prev.filter(
        (reader) => reader.serialNumber !== readerInfo.serialNumber
      ),
    ]);
  });
  useSquareEventListener(
    ReaderEventType.READER_CHANGED,
    ({ readerInfo: changedReader, change }) => {
      setPairedReaders((prev) =>
        prev.map((reader) =>
          reader.id === changedReader.id ? changedReader : reader
        )
      );
      console.log('Reader Changed: ', change);
    }
  );
  useSquareEventListener(ReaderEventType.READER_REMOVED, ({ readerInfo }) => {
    setPairedReaders((prev) =>
      prev.filter((reader) => reader.serialNumber !== readerInfo.serialNumber)
    );
  });

  const authorizeSDK = async () => {
    try {
      let response = await authorizeMobilePaymentsSdk(
        'A Square Access Token',
        'A Square Location Id'
      );
      Alert.alert(response);
    } catch (error) {
      Alert.alert((error as SquareError).message);
    }
  };

  const deauthorizeSDK = async () => {
    try {
      let response = await deauthorizeMobilePaymentsSdk();
      Alert.alert(response);
    } catch (error) {
      Alert.alert((error as SquareError).message);
    }
  };

  const startReaderPairing = async () => {
    try {
      await startPairing();
    } catch (error) {
      Alert.alert((error as SquareError).message);
    }
  };

  const stopReaderPairing = async () => {
    try {
      await stopPairing();
    } catch (error) {
      Alert.alert((error as SquareError).message);
    }
  };

  const presentReaderSettings = async () => {
    try {
      await presentSettings();
    } catch (error) {
      Alert.alert((error as SquareError).message);
    }
  };

  const getReaderLocation = async () => {
    try {
      const location = await getAuthorizationLocation();
      Alert.alert('Authorized Location', JSON.stringify(location, null, 2));
    } catch (error) {
      Alert.alert((error as SquareError).message);
    }
  };

  const getReaderAuthState = async () => {
    try {
      const state = await getAuthorizationState();
      Alert.alert('Authorization State', JSON.stringify(state, null, 2));
    } catch (error) {
      Alert.alert((error as SquareError).message);
    }
  };

  const getSquareReaders = async () => {
    try {
      let readers = await getReaders();
      setPairedReaders(readers);
      Alert.alert(
        `Found ${readers.length} paired reader${readers.length === 1 ? '' : 's'}`
      );
    } catch (error) {
      Alert.alert((error as SquareError).message);
    }
  };

  const serialNumberStyles = (reader: ReaderInfo): TextStyle => {
    return {
      fontWeight: 'bold',
      fontSize: 16,
      color:
        reader.state === ReaderState.Ready
          ? 'green'
          : reader.state === ReaderState.Connecting
            ? 'yellow'
            : 'red',
    };
  };

  const statusStyles = (reader: ReaderInfo): TextStyle => {
    return {
      color:
        reader.state === ReaderState.Ready
          ? 'green'
          : reader.state === ReaderState.Connecting
            ? 'yellow'
            : 'red',
    };
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.pairedReadersContainer}>
        {pairedReaders.length !== 0 ? (
          <>
            <Text style={styles.title}>Paired Readers</Text>
            <ScrollView
              horizontal
              contentContainerStyle={styles.scrollViewContainer}
            >
              {pairedReaders.map((reader) => (
                <View key={reader.serialNumber} style={styles.readerContainer}>
                  <Text style={serialNumberStyles(reader)}>
                    Reader {reader.serialNumber}
                  </Text>
                  <Text style={styles.text}>Name: {reader.name}</Text>
                  <Text style={styles.text}>
                    State:{' '}
                    <Text style={statusStyles(reader)}>{reader.state}</Text>{' '}
                  </Text>
                  {reader.state === ReaderState.Ready && (
                    <Text style={styles.green}>
                      Battery: {reader.batteryStatus?.percentage}%
                    </Text>
                  )}

                  <Button
                    title="Forget"
                    onPress={() => forgetReader(reader.id)}
                  />
                </View>
              ))}
            </ScrollView>
          </>
        ) : (
          <Text style={[styles.title, styles.red]}>No Paired Readers</Text>
        )}
      </View>
      <View style={styles.titleContainer}>
        <Text style={styles.title}>Authorization</Text>
      </View>
      <Button title="Authorize SDK" onPress={authorizeSDK} />
      <Button title="Deauthorize SDK" onPress={deauthorizeSDK} />

      <View style={styles.titleContainer}>
        <Text style={styles.title}>Pairing</Text>
        <Text style={styles.subtitle}>Put reader in pairing mode to pair.</Text>
      </View>
      <Button title="Start Pairing" onPress={startReaderPairing} />
      <Button title="Stop Pairing" onPress={stopReaderPairing} />
      <Button title="Present Settings" onPress={presentReaderSettings} />

      <View style={styles.titleContainer}>
        <Text style={styles.title}>Status</Text>
      </View>
      <Button title="Get Authorized Location" onPress={getReaderLocation} />
      <Button title="Get Authorization State" onPress={getReaderAuthState} />
      <Button title="Get Paired Readers" onPress={getSquareReaders} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignSelf: 'stretch',
    alignItems: 'flex-start',
    justifyContent: 'center',
    backgroundColor: 'black',
  },
  titleContainer: {
    marginVertical: 10,
    alignSelf: 'stretch',
    paddingBottom: 10,
    borderBottomColor: 'gray',
    borderBottomWidth: 1,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
  },
  subtitle: {
    fontSize: 12,
    color: 'lightgray',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
  scrollViewContainer: {
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 15,
    gap: 10,
  },
  pairedReadersContainer: {
    marginVertical: 10,
    alignSelf: 'stretch',
    paddingBottom: 10,
    borderBottomColor: 'gray',
    borderBottomWidth: 1,
  },
  readerContainer: {
    backgroundColor: '#2f2f2f',
    paddingVertical: 10,
    paddingHorizontal: 15,
    borderRadius: 10,
    alignItems: 'flex-start',
  },
  text: { color: 'white' },
  red: {
    color: 'red',
  },
  green: {
    color: 'green',
  },
});
