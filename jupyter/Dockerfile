FROM quay.io/jupyter/pyspark-notebook:latest

USER root

RUN pip install --no-cache-dir \
    plotly \
    deltalake \
    kafka-python-ng \
    kaleido

USER ${NB_UID}
