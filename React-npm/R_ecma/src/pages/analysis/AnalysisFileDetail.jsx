import React from 'react';
import { useOutletContext } from 'react-router-dom';
import './AnalysisFileDetail.css';

function AnalysisFileDetail() {

    const {
        selectedFile,
    } = useOutletContext();


    if (!selectedFile) {

        return (
            <div className="analysis-file-detail">
                <div className="analysis-file-detail__empty">
                    왼쪽에서 파일을 선택해주세요.
                </div>
            </div>
        );

    }


    const lines =
        (selectedFile.content || '').split('\n');


    const vulnerabilities =
        selectedFile.vulnerabilities || [];


    return (

        <div className="analysis-file-detail">

            {/* =========================================
                파일 헤더
            ========================================= */}

            <div className="analysis-file-detail__header">

                <div>

                    <div className="analysis-file-detail__breadcrumb">

                        파일 #{selectedFile.fileId}

                    </div>


                    <h1>
                        {selectedFile.relativePath}
                    </h1>


                    <p>
                        {selectedFile.language || 'Unknown'}
                        {' · '}
                        {lines.length} lines
                    </p>

                </div>

            </div>


            {/* =========================================
                코드
            ========================================= */}

            <div className="analysis-file-detail__card">

                <div className="analysis-file-detail__toolbar">

                    <span>
                        코드 본문
                    </span>

                    <span>
                        {lines.length} lines
                    </span>

                </div>


                <div className="analysis-file-detail__code">

                    {lines.map((line, index) => {

                        const lineNumber =
                            index + 1;


                        const vulnerability =
                            vulnerabilities.find(
                                (item) =>
                                    lineNumber >=
                                        item.startLine &&
                                    lineNumber <=
                                        item.endLine
                            );


                        return (

                            <div
                                key={lineNumber}
                                className={
                                    `analysis-file-detail__line ${
                                        vulnerability
                                            ? 'analysis-file-detail__line--finding'
                                            : ''
                                    }`
                                }
                            >

                                <span className="analysis-file-detail__line-number">
                                    {lineNumber}
                                </span>


                                <code>
                                    {line || ' '}
                                </code>


                                {vulnerability && (

                                    <span className="analysis-file-detail__marker">

                                        {vulnerability.label ||
                                            vulnerability.message ||
                                            vulnerability.type ||
                                            '취약점'}

                                    </span>

                                )}

                            </div>

                        );

                    })}

                </div>

            </div>

        </div>

    );

}

export default AnalysisFileDetail;
